Bagri .NET client produced with help of IKVM tool (https://www.ikvm.net/).
In order to build it from command line:
- install IKVM tool on your local machine
- copy the bagri-1.1.2-xqj.dll assembly from <bagri_home>\distr folder to <ivm_home>\bin
- open local pom.xml file and specify two properties:
- ikvm.home: where IKVM tool is installed
- dotnet.home: where .NET framework is installed
- build the demo app with maven: >mvn clean package
- start Bagri server somewhere in your environment
- copy the produced BagriDemo.exe app to <ikvm_home>\bin folder
- run demo app from this location