module namespace fhir = "http://hl7.org/fhir"; 
declare namespace rest = "http://www.exquery.com/restxq";
(: declare namespace bgdm = "http://bagridb.com/bagri-xdm"; :)
declare namespace p = "http://hl7.org/fhir"; 

declare 
  %rest:GET
  %rest:produces("application/xml")
function fhir:patients() as element()* {
  for $doc in fn:collection("Patients")/p:Patient
  return $doc
};

declare 
  %rest:GET
  %rest:path("/{id}")
  %rest:produces("application/xml")
function fhir:patient-by-id($id as xs:string) as element()? {
  fn:collection("Patients")/p:Patient[p:id/@value = $id]
};

(:
declare 
  %rest:GET
  %rest:path("/{uri}")
  %rest:produces("application/xml", "application/json")
function fhir:security-by-uri($uri as xs:string) as element()? {
  bgdm:get-document($uri)
};

declare 
  %rest:GET
  %rest:path("/{sym}")
  %rest:produces("application/json")
function fhir:security-by-symbol($sym as xs:string) as element()? {
  for $sec in fn:collection("CLN_Security")/s:Security
  where $sec/s:Symbol=$sym
  return $sec
};


declare 
  %rest:POST
  %rest:consumes("application/xml")
  %rest:produces("application/json")
  %rest:query-param("uri", "{$uri}", "unknown")
  %rest:matrix-param("props", "{$props}", "()")
function fhir:create-security($uri as xs:string, $content as xs:string, $props as item()*) as item()? {
  if (fn:empty($props)) then (
    bgdm:store-document(xs:anyURI($uri), $content, ())
  ) else (
    bgdm:store-document(xs:anyURI($uri), $content, $props)
  )
};


declare 
  %rest:DELETE
  %rest:path("/{uri}")
function fhir:delete-security($uri as xs:string) as item()? {
  bgdm:remove-document(xs:anyURI($uri)) 
};

:)