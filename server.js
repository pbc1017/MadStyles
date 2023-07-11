
const { MongoClient, ServerApiVersion } = require('mongodb');
const express=require('express');
const axios=require("axios");
const cheerio=require("cheerio");
const fs=require('fs')
// const prompt=require('prompt-sync')({singint:true});

var app=express();
var server=require('http').createServer(app);
app.use(express.json());
app.use(express.urlencoded({extended : false}));

app.post('/recommend/:idx',async (req,res)=>{
  try{
      await client.connect();
    userdata=client.db('Users').collection('person');
    const user=await userdata.find(req.body).toArray();
    fashiondata=client.db('Fashion').collection('Clothes');
    if(req.params.idx==0)
      result= await fashiondata.find({gender:user[0].gender}).sort({"rank":-1}).limit(10).toArray();
    else if(req.params.idx==1){
      if(user[0].prefer.color=="전체")
        result= await fashiondata.find({gender:user[0].gender}).sort({"rank":-1}).limit(10).toArray();
      else
        result= await fashiondata.find({gender:user[0].gender,color:user[0].prefer.color}).sort({"rank":-1}).limit(10).toArray();
    }
    else{
      kinds=["상의","바지","아우터","신발","가방","모자"];
      result= await fashiondata.find({kind:kinds[Math.floor(Math.random()*kinds.length)]}).sort({"rank":-1}).limit(10).toArray();
    }
    res.json(result);
  }
  finally
  {
  //   if(req.params.idx==2)
  //   client.close();
    
  }
});

app.post('/ranking/:pgnum',async (req,res)=>{
  try{
    await client.connect();
    fashiondata=client.db('Fashion').collection('Clothes');
    if(req.body.kind=="전체")
      result= await fashiondata.find({gender:req.body.gender}).skip(20*(req.params.pgnum-1)).sort({"rank":-1}).limit(20).toArray();
    else
      result= await fashiondata.find(req.body).sort({"rank":-1}).skip(20*(req.params.pgnum-1)).limit(20).toArray(); //body에 gender와 kind
    res.json(result);
  }
  finally
  {
    // client.close();
    
  }
});


app.post('/login',async (req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    const result=await userdata.find(req.body).toArray();
    if(result.length>0)
    {
      //return user info
      res.json(result[0]);
    }
    else
    //login false
      res.json("false");
  }
  finally
  {
    // client.close();
  }

});

app.post('/updateaccount',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    await userdata.updateOne({id:req.body.id},{$set:req.body.data})
    res.json("OK");
  }
  finally
  {
    // client.close();
  }
});

app.post('/getcartitems',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    const user=await userdata.find(req.body).toArray();
    fashiondata=client.db('Fashion').collection('Clothes');
    let result=[]
    for(var e of user[0].cart)
    {
      const item=await fashiondata.find({id:e.id}).toArray();
      item[0].count=e.count
      result.push(item[0])
    }
    res.json(result);
  }
  finally
  {
  }
});

/*
{
  id:userid,
  item:{
    id:clothesid
    count:n
  }
}
*/
app.post('/addcart',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
   // const user=await userdata.find(req.body.id).toArray();
    await userdata.updateOne({id:req.body.id},{$push:{cart:req.body.item}})
    res.json("OK")
  }
  finally
  {
  }
});


app.post('/deletecart',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
   // const user=await userdata.find(req.body.id).toArray();
    await userdata.updateOne({id:req.body.id},{$pull:{cart:req.body.item}})
    res.json("OK")
  }
  finally
  {
  }
});

app.post('/setcart',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
   // const user=await userdata.find(req.body.id).toArray();
    await userdata.updateOne({id:req.body.id},{$set:{cart:req.body.cart}})
    res.json("OK")
  }
  finally
  {
  }
});

app.post('/createaccount',async (req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    const result=await userdata.find({id:req.body.id}).toArray();
    if(result.length>0)
    {
      res.json("exist");
    }
    else
    {
      await userdata.insertOne(req.body);
      res.json("OK");
    }
  }
  finally
  {
    // client.close();
  }
   
});

app.post('/getDetail', async (req, res) => {
  await client.connect();
  itemData=client.db('Fashion').collection('Clothes');
  const result=await itemData.find(req.body).toArray();

  const detailUrl = result[0].detail
  // console.log(detailUrl)
  const html = await axios.get(detailUrl); // Replace with the URL you're scraping
  const $ = cheerio.load(html.data);
  const imgSrcs = [];
  $('#detail_thumb .product_thumb li img').each((i, elem) => imgSrcs.push($(elem).attr('src')));

  // console.log({result: result[0], imgSrcs: imgSrcs })
  res.json({result: result[0], imgSrcs: imgSrcs });
});

app.post('/getaiimage',async(req,res)=>{
  axios({
    method:'post',
    url:'https://a40c-34-124-255-123.ngrok-free.app/getimg',
    data:req.body,
    responseType:'stream',
    headers:{'ngrok-skip-browser-warning': 1}
  }).then(response=>{
    response.data.pipe(fs.createWriteStream('./result/img'+Date.now()+".png"))
    response.data.on('end',()=>{
      res.set('Content-Type','image/gif')
      fs.createReadStream('./result/img.png').pipe(res)
    })
  })
  .catch(err=>{
    console.log(err);
  })
  
});

app.post('/search', async (req, res) => {
  try {
    const query = req.body;
    const dbQuery = {};
    const sortQuery = {};

    for (const key in query) {
        if (key === 'sort') {
            if (query[key][0] === '인기순') {
                sortQuery.rank = -1;
            } else if (query[key][0] === '낮은 가격순') {
                sortQuery.price = 1;
            } else if (query[key][0] === '높은 가격순') {
                sortQuery.price = -1;
            }
            continue;
        }
        
        if (query[key].includes('전체')) continue;
        
        if (key == 'name') {
            let searchTerm = query.name; // "나이키"를 단어의 경계로 감쌈
            dbQuery.$or = [
                { name: { $regex: searchTerm, $options: 'i' } },
                { brand: { $regex: searchTerm, $options: 'i' } }
            ]
        }
        else {
          if (key == 'pRange') {
            array = [];
            for (i of query[key]) {
              if (i === '높음') {
                array.push("High")
              } else if (i === '중간') {
                array.push("Mid")
              } else if (i === '낮음') {
                array.push("Low")
              }
            }
            query[key] = array
          }
          dbQuery[key] = { $in: query[key] };
        }
    }
    // console.log(dbQuery)

    await client.connect();
    userdata=client.db('Fashion').collection('Clothes');
    const results = await userdata
        .find(dbQuery)
        .sort(sortQuery)
        .toArray();

    res.json(results);
  } finally {
    client.close();
  }
});

app.post('/addReview', async (req, res) => {
    await client.connect();
    const itemData = client.db('Fashion').collection('Clothes');
    
    const review = req.body.review;
    const itemId = req.body.itemId;

    await itemData.findOneAndUpdate(
        { id: itemId },
        { $pull: { review: { userId: review.userId } } }
    );

    const result = await itemData.findOneAndUpdate(
        { id: itemId },
        { $push: { review: review } },
        { returnDocument: "after" }
    );

    res.json(result);
});

app.post('/deleteReview', async (req, res) => {
    await client.connect();
    const itemData = client.db('Fashion').collection('Clothes');

    const itemId = req.body.itemId;
    const reviewId = req.body.reviewId;

    const result = await itemData.findOneAndUpdate(
        { id: itemId },
        { $pull: { reviews: { id: reviewId } } },
        { returnDocument: "after" }
    );

    res.json(result);
});
/*
app.get('/ranking/:name/:id',async (req,res)=>{
    if(req.params.name=="musinsa")
      //result=await GetFromMusinsa("https://www.musinsa.com/categories/item/"+req.params.id);//추천
      result=await GetFromMusinsa("https://www.musinsa.com/ranking/best?period=dayr&viewType=large&mainCategory="+req.params.id);
      //
    else
      result=await GetFromStyleNanda("https://stylenanda.com/product/list.html?cate_no=4259");
  res.send(result);

});
*/

server.listen(80,main);


//DB CODE

const uri = "mongodb+srv://gloveman50:zohCzGt3lh6icZKl@clustermad.qjzy8y9.mongodb.net/?retryWrites=true&w=majority";
//api key E2kpU7xTXiQrNi6WEWE6p1gNFC6dCpd4ZcMEuWHgsn0NHyc86dB3pGVSSwWED7Uz
// Create a MongoClient with a MongoClientOptions object to set the Stable API version
const client = new MongoClient(uri, {
  serverApi: {
    version: ServerApiVersion.v1,
    strict: true,
    deprecationErrors: true,
  }
});

function main() {
    //await collection.updateOne(QUERYDATA},{$set:{CHANGEDATA}})
    console.log("Server On");


}

// async function GetFromMusinsa(url) //"https://www.musinsa.com/categories/item/001001"
// {
//   let res=[]
//   const html=await axios.get(url);

//   const $=cheerio.load(html.data)
//   const clothes=$(".li_box")
//   clothes.map((i,element)=>{
//     res[i]={
//       //name:$(element).find(".img-block").attr("title"),
//       name:$(element).find(".list_info").find("a").attr("title"),
//       price:$(element).find(".txt_price_member").text(),
//       img:$(element).find("img").attr("data-original")
//     };
//     //console.log($(element).find(".item_title").text())//브랜드
//     //console.log($(element).find(".img-block").attr("href"))//상품링크
//   });
//   return res
// }

// async function GetFromStyleNanda(url) //"https://stylenanda.com/product/list.html?cate_no=4259"
// {
//   let res=[]
//   const html=await axios.get(url);
//   const $=cheerio.load(html.data)
//   const clothes=$(".column4").find("li")
//   clothes.map((i,element)=>{
//     sale=$(element).find(".table").find("span").filter((i,el)=>{return $(el).attr("class")!="price";}).text()
//     res[i]={
//         name:$(element).find(".name").find("a").text().split(':')[1].trim(),
//         price:(sale!="")?sale:$(element).find(".price").text(),
//         img:"https:"+$(element).find(".thumb").find("a").find("img").attr("src")
//     };
//     //console.log("https://stylenanda.com"+$(element).find(".name").find("a").attr("href"))//상품링크
//   });
//   return res
// }
