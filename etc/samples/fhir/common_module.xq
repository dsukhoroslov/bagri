xquery version "3.1";
module namespace comm = "http://hl7.org/fhir"; 
declare namespace rest = "http://www.expath.org/restxq";
declare namespace bgdm = "http://bagridb.com/bagri-xdm";
declare namespace m="http://www.w3.org/2005/xpath-functions/map";


declare 
  %rest:GET
  %rest:path("/metadata")
  %rest:produces("application/fhir+xml")
  %rest:query-param("_format", "{$format}") 
function comm:get-conformance-x($format as xs:string?) as item() {
  if (exists($format) and not ($format = ("application/xml", "application/fhir+xml"))) then 
    "The endpoint produce response in application/fhir+xml format, but [" || $format || "] specified"
  else 
    comm:get-conformance-xml()
};


declare 
  %rest:GET
  %rest:path("/metadata")
  %rest:produces("application/fhir+json")
  %rest:query-param("_format", "{$format}", "") 
function comm:get-conformance-j($format as xs:string?) as item() {
  if ($format = "application/fhir+xml") then 
    "The endpoint produce application/fhir+json format, wrong format specified"
  else 
    comm:get-conformance-json()
};


declare 
  %private 
function comm:get-conformance-xml() as element() {
  <CapabilityStatement xmlns="http://hl7.org/fhir">
    <id value="FhirServer"/>
    <url value="http://188.166.45.131:3030/metadata"/>
    <version value="1.1-SNAPSHOT"/>
    <name value="Bagri FHIR Server Conformance Statement"/>
    <status value="draft"/>
    <experimental value="true"/>
    <date value="{current-dateTime()}"/>
    <publisher value="Bagri Project"/>
    <contact>
        <name value="Denis Sukhoroslov"/>
        <telecom>
            <system value="email"/>
            <value value="info@bagridb.com"/>
            <use value="work"/>
        </telecom>
        <telecom>
            <system value="other"/>
            <value value="http://bagridb.com/"/>
        </telecom>
    </contact>
    <description value="Standard Conformance Statement for the open source Reference FHIR Server provided by Bagri"/>
    <kind value="instance"/>
    <instantiates value="http://hl7.org/fhir/Conformance/terminology-server"/>
    <software>
        <name value="Reference Server"/>
        <version value="1.1-SNAPSHOT"/>
        <releaseDate value="2016-10-17"/>
    </software>
    <implementation>
        <description value="FHIR Server running at http://188.166.45.131:3030/"/>
        <url value="http://188.166.45.131:3030/"/>
    </implementation>
    <fhirVersion value="1.7.0"/>
    <acceptUnknown value="both"/>
    <format value="application/fhir+xml"/>
    <format value="application/fhir+json"/>
    <rest>
        <mode value="server"/>
        <resource>
            <type value="Patient"/>
            <profile>
                <reference value="http://fhir3.healthintersections.com.au/open/StructureDefinition/patient"/>
            </profile>
            <interaction>
                <code value="read"/>
            </interaction>
            <interaction>
                <code value="vread"/>
            </interaction>
            <interaction>
                <code value="search-type"/>
            </interaction>
            <interaction>
                <code value="update"/>
            </interaction>
            <interaction>
                <code value="create"/>
            </interaction>
            <interaction>
                <code value="delete"/>
            </interaction>
            <readHistory value="true"/>
            <updateCreate value="true"/>
            <searchParam>
                <name value="birthdate"/>
                <definition value="http://hl7.org/fhir/SearchParameter/Patient-birthdate"/>
                <type value="date"/>
                <documentation value="The patient's date of birth"/>
                <modifier value="exact"/>
            </searchParam>
            <searchParam>
                <name value="gender"/>
                <definition value="http://hl7.org/fhir/SearchParameter/Patient-gender"/>
                <type value="token"/>
                <documentation value="Gender of the patient"/>
                <modifier value="exact"/>
            </searchParam>
            <searchParam>
                <name value="identifier"/>
                <definition value="http://hl7.org/fhir/SearchParameter/Patient-identifier"/>
                <type value="token"/>
                <documentation value="A patient identifier"/>
                <modifier value="contains"/>
            </searchParam>
            <searchParam>
                <name value="name"/>
                <definition value="http://hl7.org/fhir/SearchParameter/Patient-name"/>
                <type value="string"/>
                <documentation value="A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text"/>
                <modifier value="contains"/>
            </searchParam>
            <searchParam>
                <name value="telecom"/>
                <definition value="http://hl7.org/fhir/SearchParameter/Patient-telecom"/>
                <type value="token"/>
                <documentation value="The value in any kind of telecom details of the patient"/>
                <modifier value="contains"/>
            </searchParam>
        </resource>
    </rest>
  </CapabilityStatement>
};


declare 
  %private 
function comm:get-conformance-json() as item() {

  let $json := fn:parse-json('{
      "resourceType": "CapabilityStatement",
      "id": "FhirServer",
      "url": "http://188.166.45.131:3030/metadata",
      "version": "1.1-SNAPSHOT",
      "name": "Bagri FHIR Server Conformance Statement",
      "status": "draft",
      "experimental": true,
      "date": "now",
      "publisher": "Bagri Project",
      "contact": [
         {
            "name": "Denis Sukhoroslov",
            "telecom": [
               {
                  "system": "email",
                  "value": "info@bagridb.com",
                  "use": "work"
               },
               {
                  "system": "other",
                  "value": "http://bagridb.com/"
               }
            ]
         }
      ],
      "description": "Standard Conformance Statement for the open source Reference FHIR Server provided by Bagri",
      "kind": "instance",
      "instantiates": [
         "http://hl7.org/fhir/Conformance/terminology-server"
      ],
      "software": {
         "name": "Reference Server",
         "version": "1.1-SNAPSHOT",
         "releaseDate": "2016-10-17"
      },
      "implementation": {
         "description": "FHIR Server running at http://188.166.45.131:3030/",
         "url": "http://188.166.45.131:3030/"
      },
      "fhirVersion": "1.7.0",
      "acceptUnknown": "both",
      "format": [
         "application/fhir+xml",
         "application/fhir+json"
      ],
      "rest": [
         {
            "mode": "server",
            "resource": [
               {
               "type": "Patient",
               "profile": {
                  "reference": "http://fhir3.healthintersections.com.au/open/StructureDefinition/patient"
               },
               "interaction": [
                  {
                     "code": "read"
                  },
                  {
                     "code": "vread"
                  },
                  {
                     "code": "search-type"
                  },
                  {
                     "code": "update"
                  },
                  {
                     "code": "create"
                  },
                  {
                     "code": "delete"
                  }
               ],
               "readHistory": true,
               "updateCreate": true,
               "searchParam": [
                  {
                     "name": "birthdate",
                     "definition": "http://hl7.org/fhir/SearchParameter/Patient-birthdate",
                     "type": "date",
                     "documentation": "The patient''s date of birth",
                     "modifier": [
                        "missing"
                     ]
                  },
                  {
                     "name": "gender",
                     "definition": "http://hl7.org/fhir/SearchParameter/Patient-gender",
                     "type": "token",
                     "documentation": "Gender of the patient",
                     "modifier": [
                        "missing",
                        "text",
                        "in",
                        "not-in"
                     ]
                  },
                  {
                     "name": "identifier",
                     "definition": "http://hl7.org/fhir/SearchParameter/Patient-identifier",
                     "type": "token",
                     "documentation": "A patient identifier",
                     "modifier": [
                        "missing",
                        "text",
                        "in",
                        "not-in"
                     ]
                  },
                  {
                     "name": "name",
                     "definition": "http://hl7.org/fhir/SearchParameter/Patient-name",
                     "type": "string",
                     "documentation": "A server defined search that may match any of the string fields in the HumanName, including family, give, prefix, suffix, suffix, and/or text",
                     "modifier": [
                        "missing",
                        "exact",
                        "contains"
                     ]
                  },
                  {
                     "name": "telecom",
                     "definition": "http://hl7.org/fhir/SearchParameter/Patient-telecom",
                     "type": "token",
                     "documentation": "The value in any kind of telecom details of the patient",
                     "modifier": [
                        "missing",
                        "text",
                        "in",
                        "not-in"
                     ]
                  }
               ]
            }
         ],
         "interaction": [
            {
               "code": "transaction"
            },
            {
               "code": "search-system"
            }
         ]
      }
   ]
}')
  let $json := m:put($json, "date", fn:current-dateTime())
  let $props := map{'method': 'json', 'indent': fn:true()}
  return fn:serialize($json, $props)
};



(:

<Conformance xmlns="http://hl7.org/fhir"> 
 <!-- from Resource: id, meta, implicitRules, and language -->
 <!-- from DomainResource: text, contained, extension, and modifierExtension -->
 <url value="[uri]"/><!-- 0..1 Логический URI для ссылки на это заявление -->
 <version value="[string]"/><!-- 0..1 Логический идентификатор конкретной версии заявления -->
 <name value="[string]"/><!-- 0..1 Неформальное имя этого заявления о соответствии -->
 <status value="[code]"/><!-- 1..1 draft | active | retired -->
 <experimental value="[boolean]"/><!-- 0..1 Если только для тестирования, не для реального использования -->
 <date value="[dateTime]"/><!-- 1..1 Дата(/время) публикации -->
 <publisher value="[string]"/><!-- 0..1 Имя издателя (организация или частное лицо) -->
 <contact>  <!-- 0..* Контактные данные издателя -->
  <name value="[string]"/><!-- 0..1 Имя контактного лица для связи -->
  <telecom><!-- 0..* ContactPoint Контактная информация физического лица или издателя --></telecom>
 </contact>
 <description value="[markdown]"/><!-- ?? 0..1 Человекочитаемое описание заявления о соответствии -->
 <useContext><!-- 0..* CodeableConcept Содержимое предназначено для поддержки следующих видов контекста --></useContext>
 <requirements value="[markdown]"/><!-- 0..1 Для чего был создан этот ресурс -->
 <copyright value="[string]"/><!-- 0..1 Ограничения по использованию и/или публикации -->
 <kind value="[code]"/><!-- 1..1 instance | capability | requirements -->
 <instantiates value="[uri]"/><!-- 0..* Canonical URL of service implemented/used by software -->
 <software>  <!-- ?? 0..1 ПО, покрываемое этим заявлением о соответствии -->
  <name value="[string]"/><!-- 1..1 Название ПО -->
  <version value="[string]"/><!-- 0..1 Версия, на которую распространяется это заявление -->
  <releaseDate value="[dateTime]"/><!-- 0..1 Дата выпуска этой версии -->
 </software>
 <implementation>  <!-- ?? 0..1 В случае описания конкретного экземпляра -->
  <description value="[string]"/><!-- 1..1 Описывает этот конкретный экземпляр -->
  <url value="[uri]"/><!-- 0..1 Базовый URL для этой инсталляции -->
 </implementation>
 <fhirVersion value="[id]"/><!-- 1..1 Версия FHIR, которую использует система -->
 <acceptUnknown value="[code]"/><!-- 1..1 no | extensions | elements | both -->
 <format value="[code]"/><!-- 1..* Поддерживаемые форматы (xml | json | mime-тип)  -->
 <profile><!-- 0..* Reference(StructureDefinition) Поддерживаемые профили сценариев использования --></profile>
 <rest>  <!-- ?? 0..* Если точка взаимодействия является RESTful -->
  <mode value="[code]"/><!-- 1..1 client | server -->
  <documentation value="[string]"/><!-- 0..1 Общее описание реализации -->
  <security>  <!-- 0..1 Информация о безопасности реализации -->
   <cors value="[boolean]"/><!-- 0..1 Добавляет CORS-заголовки (http://enable-cors.org/) -->
   <service><!-- 0..* CodeableConcept OAuth | SMART-on-FHIR | NTLM | Basic | Kerberos | Certificates --></service>
   <description value="[string]"/><!-- 0..1 Общее описание того, как работает обеспечение безопасности -->
   <certificate>  <!-- 0..* Сертификаты, связанные с профилями безопасности -->
    <type value="[code]"/><!-- 0..1 Mime type for certificate  -->
    <blob value="[base64Binary]"/><!-- 0..1 Непосредственно сам сертификат -->
   </certificate>
  </security>
  <resource>  <!-- 0..* Ресурс, обслуживаемый в этом REST-интерфейсе -->
   <type value="[code]"/><!-- 1..1 Тип ресурса, который поддерживается -->
   <profile><!-- 0..1 Reference(StructureDefinition) Базовый системный профиль для всех пользователей ресурса --></profile>
   <documentation value="[markdown]"/><!-- 0..1 Additional information about the use of the resource type -->
   <interaction>  <!-- 1..* Какие операции поддерживаются? -->
    <code value="[code]"/><!-- 1..1 read | vread | update | delete | history-instance | history-type | create | search-type -->
    <documentation value="[string]"/><!-- 0..1 Особенности работы операции -->
   </interaction>
   <versioning value="[code]"/><!-- 0..1 no-version | versioned | versioned-update -->
   <readHistory value="[boolean]"/><!-- 0..1 Может ли vRead возвращать прошлые версии -->
   <updateCreate value="[boolean]"/><!-- 0..1 Может ли update создавать новые сущности -->
   <conditionalCreate value="[boolean]"/><!-- 0..1 Допускается/используется ли операция условного создания -->
   <conditionalRead value="[code]"/><!-- 0..1 not-supported | modified-since | not-match | full-support -->
   <conditionalUpdate value="[boolean]"/><!-- 0..1 Допускается/используется ли операция условного обновления -->
   <conditionalDelete value="[code]"/><!-- 0..1 not-supported | single | multiple - how conditional delete is supported -->
   <searchInclude value="[string]"/><!-- 0..* Значения _include, поддерживаемые сервером -->
   <searchRevInclude value="[string]"/><!-- 0..* Возможные значения параметра _revinclude, поддерживаемые сервером -->
   <searchParam>  <!-- 0..* Параметры поиска, поддерживаемые реализацией -->
    <name value="[string]"/><!-- 1..1 Имя параметра поиска -->
    <definition value="[uri]"/><!-- 0..1 Источник определения параметра -->
    <type value="[code]"/><!-- 1..1 number | date | string | token | reference | composite | quantity | uri -->
    <documentation value="[string]"/><!-- 0..1 Применение, специфичное для сервера -->
    <target value="[code]"/><!-- 0..* Типы ресурса (для ссылки на ресурс) -->
    <modifier value="[code]"/><!-- 0..* missing | exact | contains | not | text | in | not-in | below | above | type -->
    <chain value="[string]"/><!-- 0..* Поддерживемые цепочечные имена -->
   </searchParam>
  </resource>
  <interaction>  <!-- 0..* Какие операции поддерживаются? -->
   <code value="[code]"/><!-- 1..1 transaction | batch | search-system | history-system -->
   <documentation value="[string]"/><!-- 0..1 Особенности работы операции -->
  </interaction>
  <searchParam><!-- 0..* Content as for Conformance.rest.resource.searchParam Параметры поиска по всем ресурсам --></searchParam>
  <operation>  <!-- 0..* Определение операции или пользовательского запроса -->
   <name value="[string]"/><!-- 1..1 Имя для вызова этой операции/запроса -->
   <definition><!-- 1..1 Reference(OperationDefinition) Заданная операция/запрос --></definition>
  </operation>
  <compartment value="[uri]"/><!-- 0..* Логические модули (Compartments), обслуживаемые/используемые системой -->
 </rest>
 <messaging>  <!-- ?? 0..* Поддерживается ли обмен сообщениями -->
  <endpoint>  <!-- 0..* Куда сообщения должны быть отправлены -->
   <protocol><!-- 1..1 Coding http | ftp | mllp + --></protocol>
   <address value="[uri]"/><!-- 1..1 Адрес точки взаимодействия -->
  </endpoint>
  <reliableCache value="[unsignedInt]"/><!-- 0..1 Время кеширования для безотказного обмена сообщениями (мин) -->
  <documentation value="[string]"/><!-- 0..1 Подробности работы интерфейса обмена сообщениями -->
  <event>  <!-- 1..* Объявить поддержку этого события -->
   <code><!-- 1..1 Coding Тип события --></code>
   <category value="[code]"/><!-- 0..1 Consequence | Currency | Notification -->
   <mode value="[code]"/><!-- 1..1 sender | receiver -->
   <focus value="[code]"/><!-- 1..1 Ресурс в центре внимания сообщения -->
   <request><!-- 1..1 Reference(StructureDefinition) Профиль, описывающий запрос --></request>
   <response><!-- 1..1 Reference(StructureDefinition) Профиль, описывающий ответ --></response>
   <documentation value="[string]"/><!-- 0..1 Документация события, специфичная для точки взаимодействия -->
  </event>
 </messaging>
 <document>  <!-- ?? 0..* Определение документа -->
  <mode value="[code]"/><!-- 1..1 producer | consumer -->
  <documentation value="[string]"/><!-- 0..1 Описание поддержки документов -->
  <profile><!-- 1..1 Reference(StructureDefinition) Ограничение на ресурс, используемый в документе --></profile>
 </document>
</Conformance>

:)