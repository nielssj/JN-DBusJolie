include "simpleTypesInterface.iol"
include "console.iol"

outputPort SimpleTypesServer {
	Location: "dbus:/org.testname:/object"
	Interfaces: SimpleTypes
}

main 
{
	getInt@SimpleTypesServer()(intResponse);
	if (intResponse == 42) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for integer passing failed, got " + intResponse )()
	};

	getBool@SimpleTypesServer()(boolResponse);
	if (boolResponse == true) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for boolean passing failed, got " + boolResponse )()
	};

	getString@SimpleTypesServer()(stringResponse);
	if (stringResponse == "John Doe") {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for string passing failed, got " + stringResponse )()
	}
}