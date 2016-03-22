module namespace bgdm = "http://bagridb.com/bagri-xdm";
declare namespace s = "http://tpox-benchmark.com/security"; 


declare function bgdm:before-delete-security($sec as document-node(element(s:Security))) 
{
  (: 'trigger: before delete security' :)
  fn:concat("before delete security: ", $sec)
};


