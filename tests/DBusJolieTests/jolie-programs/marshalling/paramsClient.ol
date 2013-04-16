include "paramsInterface.iol"
include "console.iol"

outputPort ParamsServer {
	Location: "dbus:/net.jolie.params:/paramsServer"
	Interfaces: Params
}

main 
{
	argument.params[0] = 42;
	argument.params[1] = "John Doe";
	argument.params[2].field1 = true;
	argument.params[2].field2 = false;
	argument.params[3].field1[0] = 0;
	argument.params[3].field1[1] = 1;

	testParams@ParamsServer( argument )( response );

	if (response.params[0] == 42) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 0 passing failed, got " + response.params[0] )()
	};

	if (response.params[1] == "John Doe") {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 1 passing failed, got " + response.params[1] )()
	};

	if (response.params[2].field1 == true) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 2 field1 passing failed, got " + response.params[2].field1 )()
	};

	if (response.params[2].field2 == false) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 2 field2 passing failed, got " + response.params[2].field2 )()
	};

	if (response.params[3].field1[0] == 0) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 3 field1, index 0 passing failed, got " + response.params[3].field1[0] )()
	};

	if (response.params[3].field1[1] == 1) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for params 3 field1, index 1 passing failed, got " + response.params[3].field1[1] )()
	}
}