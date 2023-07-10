
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
    else if(req.params.idx==1)
      result= await fashiondata.find({gender:user[0].gender,color:user[0].prefer.color}).sort({"rank":-1}).limit(10).toArray();
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
    for(var id of user[0].cart)
    {
      const item=await fashiondata.find({id:id}).toArray();
      result.push(item[0])
    }
    res.json(result);
  }
  finally
  {
    // client.close();
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

app.post('/getaiimage',async(req,res)=>{
  axios({
    method:'post',
    url:'https://a40c-34-124-255-123.ngrok-free.app/getimg',
    data:req.body,
    responseType:'stream',
    headers:{'ngrok-skip-browser-warning': 1}
  }).then(response=>{
    response.data.pipe(fs.createWriteStream('./result/img.png'))
    response.data.on('end',()=>{
      res.set('Content-Type','image/gif')
      fs.createReadStream('./result/img.png').pipe(res)
    })
  })
  .catch(err=>{
    console.log(err);
  })
  
})
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
