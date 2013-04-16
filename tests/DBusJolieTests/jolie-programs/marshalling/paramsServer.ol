include "paramsInterface.iol"
include "console.iol"

inputPort ParamsServer {
	Location: "dbus:/net.jolie.params:/paramsServer"
	Interfaces: Params
}

execution { single }

main
{
	[ testParams ( request ) ( response )  {
		if (request.params[0] == 42) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 0 passing failed, got " + request.params[0] )()
		};

		if (request.params[1] == "John Doe") {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 1 passing failed, got " + request.params[1] )()
		};

		if (request.params[2].field1 == true) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 2 field1 passing failed, got " + request.params[2].field1 )()
		};

		if (request.params[2].field2 == false) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 2 field2 passing failed, got " + request.params[2].field2 )()
		};

		if (request.params[3].field1[0] == 0) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 3 field1, index 0 passing failed, got " + request.params[3].field1[0] )()
		};

		if (request.params[3].field1[1] == 1) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for params 3 field1, index 1 passing failed, got " + request.params[3].field1[1] )()
		};

		// Copy the full request and return it
		response << request
	} ] {nullProcess}
}