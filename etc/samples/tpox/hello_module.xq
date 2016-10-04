module namespace hw = "http://helloworld";
(: declare namespace fw = "java:java.io.FileWriter"; :)
(: declare namespace dbl = "java.lang.Double"; :)
declare namespace rest = "http://www.exquery.com/restxq";

declare function hw:hello($world) {
 'Hello ' (: || $world :)
};

declare function hw:helloworld($name as xs:string) as xs:string {
  fn:concat("hello ", $name, "!")
};

declare function hw:order-value($po as element(purchase-order))
   as xs:double {
      fn:sum($po/order-item/(@price * @quantity))
};

(:
declare function hw:hello_double($num as xs:string) as xs:double {
  dbl:valueOf($num)
};
:)

declare %rest:path("/hello/world")
function hw:hello_world($world as xs:string) as xs:string {
  "hello " || $world
};

(:
declare function hw:write_to_file($filename as xs:string) {
  $file := fw:new($filename)
  fw:write($file, "Hello World!")
  fw:close($file) 
  return xs:boolean(1)
};
:)

