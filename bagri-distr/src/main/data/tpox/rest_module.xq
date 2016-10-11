module namespace tpox = "http://tpox-benchmark.com/rest";
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace s="http://tpox-benchmark.com/security";
declare namespace bgdm="http://bagridb.com/bagri-xdm";

declare 
  %rest:GET
function tpox:securities() as element()* {
  for $sec in fn:collection("CLN_Security")/s:Security
  return $sec
};

(:
declare 
  %rest:GET
  %rest:path("/{uri}")
  %rest:produces("application/xml", "application/json")
function tpox:security-by-uri($uri as xs:string) as element()? {
  bgdm:get-document($uri)
};
:)

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


declare 
  %rest:POST
  %rest:consumes("application/xml")
  %rest:produces("application/json")
  %rest:query-param("uri", "{$uri}", "unknown")
  %rest:matrix-param("props", "{$props}", "()")
function tpox:create-security($uri as xs:string, $content as xs:string, $props as item()*) as item()? {
  if (fn:empty($props)) then (
    bgdm:store-document(xs:anyURI($uri), $content, ())
  ) else (
    bgdm:store-document(xs:anyURI($uri), $content, $props)
  )
};


declare 
  %rest:DELETE
  %rest:path("/{uri}")
function tpox:delete-security($uri as xs:string) as item()? {
  bgdm:remove-document(xs:anyURI($uri)) 
};

