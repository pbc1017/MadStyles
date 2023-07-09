
const { MongoClient, ServerApiVersion } = require('mongodb');
const express=require('express');
const axios=require("axios");
const cheerio=require("cheerio");
const prompt=require('prompt-sync')({singint:true});

var app=express();
var server=require('http').createServer(app);
app.use(express.json());
app.use(express.urlencoded({extended : false}));


app.get('/userlist',async(req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    const result=await userdata.find().toArray();
    res.json(result);
  }
  finally
  {
    client.close();
  }
});

app.post('/createid',async (req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    await userdata.insertOne(req.body);
  }
  finally
  {
    client.close();
    res.json("OK");
  }
   
});

app.post('/addfashion',async (req,res)=>{
  try{
    await client.connect();
    fashiondata=client.db('Fashion').collection('Clothes');
    await fashiondata.insertOne(req.body);
  }
  finally
  {
    client.close();
    res.json("OK");
  }
   
});

// app.post('/ranking',async (req,res)=>{
//   try{
//     await client.connect();
//     const user=await client.db('Users').collection('person').find(req.body).toArray();
//     fashiondata=client.db('Fashion').collection('Clothes');
//     const result= await fashiondata.find({gender:user[0].gender}).sort({"rank":-1}).limit(20).toArray();
//     res.json(result);
//   }
//   finally
//   {
//     client.close();
    
//   }
// });

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
    client.close();
    
  }
});

app.post('/ranking/:gender/:pgnum',async (req,res)=>{
  try{
    await client.connect();
    //const user=await client.db('Users').collection('person').find(req.body).toArray();
    fashiondata=client.db('Fashion').collection('Clothes');
    const result= await fashiondata.find({gender:req.params.gender}).sort({"rank":-1}).skip(20*(req.params.pgnum-1)).limit(20).toArray();
    res.json(result);
  }
  finally
  {
    client.close();
    
  }
});

// app.post('/changeinfo',async (req,res)=>{
//   try{
//     await client.connect();
//     const user=await client.db('Users').collection('person').find(req.body).toArray();
//     fashiondata=client.db('Fashion').collection('Clothes');
//     const result= await fashiondata.find({color:user[0].prefer.color}).toArray();
//     res.json(result);
//   }
//   finally
//   {
//     client.close();
    
//   }
   
// });

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
    client.close();
  }

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

    // Connect the client to the server	(optional starting in v4.7)
    //.toArray()
    //await collection.updateOne(QUERYDATA},{$set:{CHANGEDATA}})
    console.log("Server On");


}

async function GetFromMusinsa(url) //"https://www.musinsa.com/categories/item/001001"
{
  let res=[]
  const html=await axios.get(url);

  const $=cheerio.load(html.data)
  const clothes=$(".li_box")
  clothes.map((i,element)=>{
    res[i]={
      //name:$(element).find(".img-block").attr("title"),
      name:$(element).find(".list_info").find("a").attr("title"),
      price:$(element).find(".txt_price_member").text(),
      img:$(element).find("img").attr("data-original")
    };
    //console.log($(element).find(".item_title").text())//브랜드
    //console.log($(element).find(".img-block").attr("href"))//상품링크
  });
  return res
}

async function GetFromStyleNanda(url) //"https://stylenanda.com/product/list.html?cate_no=4259"
{
  let res=[]
  const html=await axios.get(url);
  const $=cheerio.load(html.data)
  const clothes=$(".column4").find("li")
  clothes.map((i,element)=>{
    sale=$(element).find(".table").find("span").filter((i,el)=>{return $(el).attr("class")!="price";}).text()
    res[i]={
        name:$(element).find(".name").find("a").text().split(':')[1].trim(),
        price:(sale!="")?sale:$(element).find(".price").text(),
        img:"https:"+$(element).find(".thumb").find("a").find("img").attr("src")
    };
    //console.log("https://stylenanda.com"+$(element).find(".name").find("a").attr("href"))//상품링크
  });
  return res
}

function AutoAdd(){

}
