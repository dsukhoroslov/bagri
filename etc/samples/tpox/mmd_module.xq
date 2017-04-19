module namespace mmd = "http://bagridb.com/mmd";
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace bgdb="http://bagridb.com/bdb";


declare 
  %rest:GET
  %rest:query-param("location", "{$location}")
  %rest:query-param("params", "{$params}")
  %rest:produces("application/xml")
function mmd:places-at-location($location as xs:string, $params as xs:string*) as item()* {
  (: TODO: set params into url :)
  let $uri := "https://maps.googleapis.com/maps/api/place/textsearch/xml?query=" || $location || "&amp;key=AIzaSyA_q3ggoJXbaKo_fFHWUAdqtoiXGrxvZ4c"
  (: set any headers if needed :)
  let $response := bgdb:http-get(xs:anyURI($uri), ())
  let $place := fn:parse-xml($response)
  let $coords := $place/PlaceSearchResponse/result/geometry/location/lat/text() || "," || $place/PlaceSearchResponse/result/geometry/location/lng/text()

  let $uri := "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=" || $coords || "&amp;radius=1000&amp;key=AIzaSyA_q3ggoJXbaKo_fFHWUAdqtoiXGrxvZ4c"
  return bgdb:http-get(xs:anyURI($uri), ())
};


