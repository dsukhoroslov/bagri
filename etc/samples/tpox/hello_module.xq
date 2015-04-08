module namespace hw = "http://helloworld";

declare function hw:helloworld($name as xs:string)
{
  fn:concat("hello ", $name, "!")
};
