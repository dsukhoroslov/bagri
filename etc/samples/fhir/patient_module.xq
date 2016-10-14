module namespace fhir = "http://hl7.org/fhir/patient"; 
declare namespace rest = "http://www.exquery.com/restxq";
declare namespace bgdm = "http://bagridb.com/bagri-xdm";
declare namespace p = "http://hl7.org/fhir"; 


declare 
  %rest:GET
  %rest:path("/{id}")
  %rest:produces("application/fhir+xml")
(:  %rest:query-param("_format", "{$format}", "") :)
  %rest:query-param("_summary", "{$summary}", "") 
function fhir:get-patient-by-id($id as xs:string, (: $format as xs:string?, :) $summary as xs:string?) as element()? {
  collection("Patients")/p:Patient[p:id/@value = $id]
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
  %rest:matrix-param("parameters", "{$parameters}", "()")
  %rest:query-param("_format", "{$format}", "") 
function fhir:get-patients($parameters as item()*, $format as xs:string?) as element()* {

  let $itr := collection("Patients")/p:Patient 
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
      {for $doc in $itr
       return 
         <entry>
           <resource>{$doc}</resource>
         </entry>
      }
    </Bundle>
};         



declare 
  %rest:POST
  %rest:path("_search")
  %rest:produces("application/fhir+xml")
  %rest:form-param("parameters", "{$parameters}", "()")
  %rest:query-param("_format", "{$format}", "") 
function fhir:search-patients($parameters as item()*, $format as xs:string?) as element()? {
  for $doc in collection("Patients")/p:Patient
  return $doc
};



declare 
  %rest:POST
  %rest:consumes("application/fhir+xml")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}", "") 
function fhir:create-patient($content as xs:string, $format as xs:string?) as element()? {
  let $doc := parse-xml($content) 
  let $uri := xs:string($doc/p:Patient/p:id/@value) || ".xml"
(:  let $out := bgdm:log-output("start doc store; got uri: " || $uri, "info") :)
  let $uri := bgdm:store-document(xs:anyURI($uri), $content, ())
(:  let $out := bgdm:log-output("doc stored; got id: " || $id, "info") :)
  let $content := bgdm:get-document($uri)
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
  let $uri := bgdm:store-document(xs:anyURI($id), $content, ())
  return collection("Patients")/p:Patient[p:id/@value = $id] 
};



declare 
  %rest:DELETE
  %rest:path("/{id}")
function fhir:delete-patient($id as xs:string) as item()? {
(:  let $doc := collection("Patients")/p:Patient[p:id/@value = $id] :)
  let $uri := bgdm:remove-document(xs:anyURI($id)) 
  return ()
};


(:

Общие параметры, определённые для всех ресурсов:
_id	token	Идентификатор ресурса (а не полный URL)	Resource.id
_lastUpdated	date	Дата последнего обновления. Сервер может по своему усмотрению устанавливать границы точности	Resource.meta.lastUpdated
_tag	token	Поиск по тегу ресурса	Resource.meta.tag
_profile	uri	Поиск всех ресурсов, помеченных профилем	Resource.meta.profile
_security	token	Поиск по метке уровня безопасности	Resource.meta.security
_text	string	Текстовый поиск по описательной части	
_content	string	Текстовый поиск по всему ресурсу целиком	
_list	string	Все ресурсы в названном списке (по идентификатору, а не полному URL)	
_query	string	Custom named query	
Search Control Parameters:
Имя	Тип	Описание	Допустимое содержимое
_sort	string	Порядок сортировки результатов (может повторяться для внутренних порядков сортировки)	Имя допустимого параметра поиска
_count	number	Количество результатов на странице	Общее количество
_include	string	Другие ресурсы для включения в результаты поиска, на которые указывают найденные при поиске совпадения	SourceType:searchParam(  :targetType)
_revinclude	string	Другие ресурсы для включения в результаты поиска, когда они ссылаются на найденные при поиске совпадения	SourceType:searchParam(  :targetType)
_summary	string	Просто верните суммарные элементы (для ресурсов, где это определено)	true | false (false is default)
_contained	string	Возвращать ли ресурсы, вложенные в другие ресурсы при поиске совпадений	true | false | both (false is default)
_containedType	string	Возвращать ли вложенные или родительские ресурсы при возвращении вложенных ресурсов	container | contained

Patient
active	token	Активна ли данная запись о пациенте	Patient.active
address	string	A server defined search that may match any of the string fields in the Address, including line, city, state, country, postalCode, and/or text	Patient.address
address-city	string	Город, указанный в адресе	Patient.address.city
address-country	string	Страна, указанная в адресе	Patient.address.country
address-postalcode	string	Почтовый индекс, указанный в адресе	Patient.address.postalCode
address-state	string	Штат, указанный в адресе	Patient.address.state
address-use	token	Код применения, указанный в адресе	Patient.address.use
animal-breed	token	Порода для пациентов-животных	Patient.animal.breed
animal-species	token	Вид для пациентов-животных	Patient.animal.species
birthdate	date	Дата рождения пациента	Patient.birthDate
death-date	date	Была указана дата смерти, или удовлетворяет данному искомому значению	Patient.deceased.as(DateTime)
deceased	token	Этот пациент помечен как умерший, либо введена дата смерти	Patient.deceased.exists()
email	token	Адрес электронной почты	Patient.telecom.where(system='email')
family	string	Часть фамилии пациента	Patient.name.family
gender	token	Пол пациента	Patient.gender
general-practitioner	reference	Patient's nominated general practitioner, not the organization that manages the record	Patient.generalPractitioner
given	string	Часть имени пациента	Patient.name.given
identifier	token	Идентификатор пациента	Patient.identifier
language	token	Код языка (безотносительно значения вида использования)	Patient.communication.language
link	reference	Все пациенты, связанные с данным пациентом	Patient.link.other
name	string	A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text	Patient.name
organization	reference	Организация, в которой этот человек является пациентом	Patient.managingOrganization
phone	token	Номер телефона	Patient.telecom.where(system='phone')
phonetic	string	Часть либо фамилии, либо имени, используя некоторый алгоритм фонетического соответствия	Patient.name
telecom	token	Значение в любом виде контактных данных пациента	Patient.telecom
race	token	Returns patients with a race extension matching the specified code.	
ethnicity	token	Returns patients with an ethnicity extension matching the specified code.	

:)