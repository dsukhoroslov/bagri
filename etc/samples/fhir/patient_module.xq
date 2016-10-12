module namespace fhir = "http://hl7.org/fhir"; 
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace bgdm = "http://bagridb.com/bagri-xdm";
declare namespace p = "http://hl7.org/fhir"; 

declare 
  %rest:GET
  %rest:produces("application/fhir+xml")
function fhir:get-patients() as element()* {
  for $doc in fn:collection("Patients")/p:Patient
  return $doc
};


declare 
  %rest:GET
  %rest:path("/{id}")
  %rest:produces("application/fhir+xml")
(:  %rest:query-param("_format", "{$format}", "") :)
  %rest:query-param("_summary", "{$summary}", "") 
function fhir:get-patient-by-id($id as xs:string, (: $format as xs:string?, :) $summary as xs:string?) as element()? {
  fn:collection("Patients")/p:Patient[p:id/@value = $id]
};


declare 
  %rest:GET
  %rest:path("/{id}/_history/{vid}")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:get-patient-by-id-version($id as xs:string, $vid as xs:string, $format as xs:string?) as element()? {
  fn:collection("Patients")/p:Patient[p:id/@value = $id]
};


declare 
  %rest:GET
  %rest:produces("application/fhir+xml")
  %rest:matrix-param("props", "{$props}", "()")
  %rest:query-param("_format", "{$format}", "") 
function fhir:search-patients($props as item()*, $format as xs:string?) as element()* {
(: build query here? pass it to QueryManager? :)
  for $doc in fn:collection("Patients")/p:Patient
  return $doc
};


declare 
  %rest:POST
  %rest:consumes("application/fhir+xml")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:create-patient($content as xs:string, $format as xs:string?) as element()? {
  let $uri := "xxx"
  let $id := bgdm:store-document(xs:anyURI($uri), $content, ())
  return fn:collection("Patients")/p:Patient[p:id/@value = $id] 
};


declare 
  %rest:PUT
  %rest:path("/{id}")
  %rest:consumes("application/fhir+xml")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:update-patient($id as xs:string, $content as xs:string, $format as xs:string?) as element()? {
  let $uri := bgdm:store-document(xs:anyURI($id), $content, ())
  return fn:collection("Patients")/p:Patient[p:id/@value = $id] 
};

(:
declare 
  %rest:DELETE
  %rest:path("/{id}")
function fhir:delete-patient($id as xs:string) as () {
  bgdm:remove-document(xs:anyURI($id)) 
};
:)

