
const { MongoClient, ServerApiVersion } = require('mongodb');
const express=require('express');
const axios=require("axios");
const cheerio=require("cheerio");

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
    await userdata.insertOne({id:req.body.id,
      password:req.body.password,
      prefer:{
        color:req.body.color
      }
    });
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
    await fashiondata.insertOne({
      name:req.body.name,
      price:req.body.price,
      imgurl:req.body.imgurl,
      color:req.body.color
    });
  }
  finally
  {
    client.close();
    res.json("OK");
  }
   
});

app.post('/requestmain',async (req,res)=>{
  try{
    await client.connect();
    const user=await client.db('Users').collection('person').find(req.body).toArray();
    fashiondata=client.db('Fashion').collection('Clothes');
    const result= await fashiondata.find({color:user[0].prefer.color}).toArray();
    res.json(result);
  }
  finally
  {
    client.close();
    
  }
   
});

app.post('/changeinfo',async (req,res)=>{
  try{
    await client.connect();
    const user=await client.db('Users').collection('person').find(req.body).toArray();
    fashiondata=client.db('Fashion').collection('Clothes');
    const result= await fashiondata.find({color:user[0].prefer.color}).toArray();
    res.json(result);
  }
  finally
  {
    client.close();
    
  }
   
});

app.post('/login',async (req,res)=>{
  try{
    await client.connect();
    userdata=client.db('Users').collection('person');
    const result=await userdata.find({id:req.body.id}).toArray();
    if(result.length>0)
    {
      //login succeed
      res.json("true");
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

server.listen(4444,main);


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
    //await collection.updateOne({QUERYDATA},{$set:{CHANGEDATA}})
    console.log("Server On");

}

async function Musinsa()
{
  const html=await axios.get("https://www.musinsa.com/categories/item/018");

  const $=cheerio.load(html.data)
  const clothes=$(".li_box")
  clothes.map((i,element)=>{
    console.log($(element).find(".item_title").text())//브랜드
    console.log($(element).find(".txt_price_member").text())//가격
    console.log($(element).find(".img-block").attr("title"))//상품명
    console.log($(element).find(".img-block").attr("href"))//상품링크
    console.log($(element).find("img").attr("data-original"))//이미지
    console.log("---------------------------------------------------------")
  });
}

async function Another()
{
  const html=await axios.get("https://www.musinsa.com/categories/item/018");

  const $=cheerio.load(html.data)
  const clothes=$(".li_box")
  clothes.map((i,element)=>{
    console.log($(element).find(".item_title").text())//브랜드
    console.log($(element).find(".txt_price_member").text())//가격
    console.log($(element).find(".img-block").attr("title"))//상품명
    console.log($(element).find(".img-block").attr("href"))//상품링크
    console.log($(element).find("img").attr("data-original"))//이미지
    console.log("---------------------------------------------------------")
  });
}
//Musinsa()


