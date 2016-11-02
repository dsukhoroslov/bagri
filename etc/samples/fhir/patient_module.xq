module namespace fhir = "http://hl7.org/fhir/patient"; 
declare namespace http = "http://www.exquery.com/http";
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace bgdm = "http://bagridb.com/bagri-xdm";
declare namespace p = "http://hl7.org/fhir"; 


declare 
  %rest:GET
  %rest:path("/{id}")
  %rest:produces("application/fhir+xml")
(:  %rest:query-param("_format", "{$format}", "") :)
  %rest:query-param("_summary", "{$summary}", "") 
function fhir:get-patient-by-id($id as xs:string, (: $format as xs:string?, :) $summary as xs:string?) as element()* {
  let $itr := collection("Patients")/p:Patient[p:id/@value = $id]
  return
    if ($itr) then 
      for $ptn in $itr
        return 
          (<rest:response>
             <http:response status="200">
             {if ($ptn/p:Patient/p:meta/p:versionId/@value) then (
               let $loc := "/Patient/" || $id || "/_history/" || $ptn/p:Patient/p:meta/p:versionId/@value
               <http:header name="ETag" value="{$ptn/p:Patient/p:meta/p:versionId/@value}"/>
(:               <http:header name="Content-Location" value="/Patient/{$id}/_history/{$ptn/p:Patient/p:meta/p:versionId/@value}"/> :)
              ) else (
               let $loc := "/Patient/" || $id
(:               <http:header name="Content-Location" value="/Patient/{$id}"/> :)
              )}
               <http:header name="Content-Location" value="{$loc}"/>
               <http:header name="Last-Modified" value="{$ptn/p:Patient/p:meta/p:lastUpdated/@value}"/>
             </http:response>                     
           </rest:response>, $ptn)
    else 
      <rest:response>
        <http:response status="404" message="Patient with id={$id} was not found."/>
      </rest:response>
(: TODO: add summary.. do we really need it on a single Patient resource? :)
};


declare 
  %rest:GET
  %rest:path("/{id}/_history/{vid}")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:get-patient-by-id-version($id as xs:string, $vid as xs:string, $format as xs:string?) as element()? {
  collection("Patients")/p:Patient[p:id/@value = $id and p:meta/p:versionId/@value = $vid]
};


declare 
  %rest:GET
  %rest:produces("application/fhir+xml")
  %rest:query-param("identifier", "{$identifier}")
  %rest:query-param("birthdate", "{$birthdate}")
  %rest:query-param("gender", "{$gender}") 
  %rest:query-param("name", "{$name}")
  %rest:query-param("telecom", "{$telecom}")
function fhir:get-patients($identifier as xs:string?, $birthdate as xs:date?, $gender as xs:string?, $name as xs:string?, $telecom as xs:string?) as element()* {

  let $itr := collection("Patients")/p:Patient[ 
	(not(exists($gender)) or p:gender/@value = $gender)
    and (not(exists($birthdate)) or p:birthDate/@value = $birthdate) 
    and (not(exists($name)) or contains(data(p:text), $name)) 
    and (not(exists($identifier)) or contains(p:identifier/p:value/@value, $identifier)) 
    and (not(exists($telecom)) or contains(string-join(p:telecom/p:value/@value, " "), $telecom))] 

  return
    <Bundle xmlns="http://hl7.org/fhir">
      <id value="{bgdm:get-uuid()}" />
      <meta>
        <lastUpdated value="{current-dateTime()}" />
      </meta>
      <type value="searchset" />
      <total value="{count($itr)}" />
      <link>
        <relation value="self" />
        <url value="http://bagridb.com/Patient/search?name=test" />
      </link>
      {for $ptn in $itr
       return 
         <entry>
           <resource>{$ptn}</resource>
         </entry>
      }
    </Bundle>
};         


declare 
  %rest:POST
  %rest:path("_search")
  %rest:produces("application/fhir+xml")
  %rest:form-param("identifier", "{$identifier}")
  %rest:form-param("birthdate", "{$birthdate}")
  %rest:form-param("gender", "{$gender}") 
  %rest:form-param("name", "{$name}")
  %rest:form-param("telecom", "{$telecom}")
function fhir:search-patients($identifier as xs:string?, $birthdate as xs:date?, $gender as xs:token?, $name as xs:string?, $telecom as xs:string?) as element()* {

  let $itr := collection("Patients")/p:Patient[ 
	(not(exists($gender)) or p:gender/@value = $gender)
    and (not(exists($birthdate)) or p:birthDate/@value = $birthdate) 
    and (not(exists($name)) or contains(data(p:text), $name)) 
    and (not(exists($identifier)) or contains(p:identifier/p:value/@value, $identifier)) 
    and (not(exists($telecom)) or contains(string-join(p:telecom/p:value/@value, " "), $telecom))] 

  return
    <Bundle xmlns="http://hl7.org/fhir">
      <id value="{bgdm:get-uuid()}" />
      <meta>
        <lastUpdated value="{current-dateTime()}" />
      </meta>
      <type value="searchset" />
      <total value="{count($itr)}" />
      <link>
        <relation value="self" />
        <url value="http://bagridb.com/Patient/search?name=test" />
      </link>
      {for $ptn in $itr
       return 
         <entry>
           <resource>{$ptn}</resource>
         </entry>
      }
    </Bundle>
};


declare 
  %rest:POST
  %rest:consumes("application/fhir+xml")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:create-patient($content as xs:string, $format as xs:string?) as element()? {
  let $doc := parse-xml($content) 
  let $uri := xs:string($doc/p:Patient/p:id/@value) || ".xml"
  let $uri := bgdm:store-document(xs:anyURI($uri), $content, ())
  let $content := bgdm:get-document-content($uri)
  let $doc := parse-xml($content)
  return $doc/p:Patient
};


declare 
  %rest:PUT
  %rest:path("/{id}")
  %rest:consumes("application/fhir+xml")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:update-patient($id as xs:string, $content as xs:string, $format as xs:string?) as element()? {
  for $uri in fhir:get-patient-uri($id)
  let $uri2 := bgdm:store-document($uri, $content, ())
  let $content2 := bgdm:get-document-content($uri2, ())
  let $doc := parse-xml($content2) 
  return $doc/p:Patient
};


declare 
  %rest:DELETE
  %rest:path("/{id}")
function fhir:delete-patient($id as xs:string) as item()? {
  for $uri in fhir:get-patient-uri($id)
  return bgdm:remove-document($uri) 
};


declare 
  %private
function fhir:get-patient-uri($id as xs:string) as xs:anyURI? {
  let $query := 
' declare namespace p = "http://hl7.org/fhir"; 
  declare variable $id external;

  for $ptn in fn:collection("Patients")/p:Patient
  where $ptn/p:id/@value = $id
  return $ptn'

  let $uri := bgdm:query-document-uris($query, ("id", $id), ())
  return xs:anyURI($uri)
};


declare 
  %private
function fhir:get-patient-uris($params as xs:string*) as xs:anyURI* {
  let $prolog := 
' declare namespace p = "http://hl7.org/fhir"; 
  declare variable $id external;
'

  let $query := 
' for $ptn in fn:collection("Patients")/p:Patient
  where $ptn/p:id/@value = $id
  return $ptn'

  let $query := $prolog || $query
  let $uri := bgdm:query-document-uris($query, $params, ())
  return xs:anyURI($uri)
};


(:

Search Control Parameters:
Имя	Тип	Описание	Допустимое содержимое
_summary	string	Просто верните суммарные элементы (для ресурсов, где это определено)	true | false (false is default)

Patient
birthdate	date	Дата рождения пациента	Patient.birthDate
gender	token	Пол пациента	Patient.gender
name	string	A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text	Patient.name
telecom	token	Значение в любом виде контактных данных пациента	Patient.telecom
identifier	Identifier	Patient.active


inject; exp: "Patients"; env: net.sf.saxon.query.QueryModule@42dfc; construct: 0; qName: null
inject; exp: collection("Patients"); env: net.sf.saxon.query.QueryModule@42dfc; construct: 2009; qName: fn:collection
inject; exp: $ptn; env: net.sf.saxon.query.QueryModule@42dfc; construct: 2014; qName: null
injectClause; traget: for $ptn in homCheck((collection("Patients"))/child::element(Q{http://hl7.org/fhir}Patient)); env: net.sf.saxon.query.QueryModule@42dfc
injectClause; traget: where (homCheck((homCheck($ptn/child::element(Q{http://hl7.org/fhir}id)))/attribute::attribute(Q{}value))) = $Q{}id; env: net.sf.saxon.query.QueryModule@42dfc

:)