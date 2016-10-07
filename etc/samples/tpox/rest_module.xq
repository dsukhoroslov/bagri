module namespace tpox = "http://tpox-benchmark.com/rest";
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace s="http://tpox-benchmark.com/security";

declare 
  %rest:GET
function tpox:securities() as element()* {
  for $sec in fn:collection("CLN_Security")/s:Security
  return $sec
};


declare 
  %rest:GET
  %rest:path("/{sym}")
  %rest:produces("application/json")
function tpox:security-by-symbol($sym as xs:string) as element()? {
  for $sec in fn:collection("CLN_Security")/s:Security
  where $sec/s:Symbol=$sym
  return $sec
};


declare 
  %rest:GET
  %rest:path("/{id}")
  %rest:produces("application/xml")
function tpox:security-by-id($id as xs:int) as element()? {
  fn:collection("CLN_Security")/s:Security[@id = $id]
};

