include "paramsInterface.iol"
include "console.iol"

outputPort ParamsServer {
	Location: "dbus:/net.jolie.params:/paramsServer"
	Interfaces: Params
}

main 
{
	argument.intValue = 42;
	argument.stringValue = "John Doe";
	//argument.params[2].field1 = true;
	//argument.params[2].field2 = false;
	argument.intArray[0] = 0;
	argument.intArray[1] = 1;

	testParams@ParamsServer( argument )( response );

	if (response.intValue == 42) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intValue passing failed, got " + response.intValue )()
	};

	if (response.stringValue == "John Doe") {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for stringValue passing failed, got " + response.stringValue)()
	};

	/*if (response.params[2].field1 == true) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 2 field1 passing failed, got " + response.params[2].fibeld1 )()
	};

	if (response.params[2].field2 == false) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 2 field2 passing failed, got " + response.params[2].field2 )()
	};
*/
	if (response.intArray[0] == 0) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intArray, index 0 passing failed, got " + response.params[3].field1[0] )()
	};

	if (response.intArray[1] == 1) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intArray, index 1 passing failed, got " + response.params[3].field1[1] )()
	}
}