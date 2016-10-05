module namespace tpox = "http://tpox-benchmark.com/rest";
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace s="http://tpox-benchmark.com/security";

declare 
  %rest:GET
  %rest:path("/tpox")
  %rest:produces("application/xml", "application/json")
function tpox:securities() as document-node()* {
  for $sec in fn:collection("CLN_Security")/s:Security
  return $sec
};


declare 
  %rest:GET
  %rest:path("/tpox/{$sym}")
  %rest:produces("application/xml", "application/json")
function tpox:security-by-symbol($sym as xs:string) as document-node()? {
  for $sec in fn:collection("CLN_Security")/s:Security
  where $sec/s:Symbol=$sym
  return $sec
};


declare 
  %rest:GET
  %rest:path("/tpox/{$id}")
  %rest:produces("application/xml", "application/json")
function tpox:security-by-id($id as xs:int) as document-node()? {
  fn:collection("CLN_Security")/s:Security[@id = $id]
};

