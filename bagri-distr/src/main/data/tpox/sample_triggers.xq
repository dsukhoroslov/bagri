module namespace bgdb = "http://bagridb.com/bdb";
declare namespace s = "http://tpox-benchmark.com/security"; 


declare function bgdb:before-delete-security($sec as document-node(element(s:Security))) 
{
  (: 'trigger: before delete security' :)
  fn:concat("before delete security: ", $sec)
};


