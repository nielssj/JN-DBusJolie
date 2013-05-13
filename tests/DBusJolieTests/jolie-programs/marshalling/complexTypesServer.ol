include "complexTypesInterface.iol"
include "console.iol"

inputPort ComplexServer {
	Location: "dbus:/net.jolie.complex:/complexServer"
	Interfaces: Complex
}

main
{
	[ testParams ( request ) ( response )  {
		if (request.intValue == 42) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intValue failed, got " + request.intValue )()
		};

		if (request.stringValue == "John Doe") {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for stringValue failed, got " + request.stringValue)()
		};

		if (request.boolMap.trueValue == true) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for pboolMap trueValue failed, got " + request.boolMap.trueValue )()
		};

		if (request.boolMap.falseValue == false) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for boolMap falseValue failed, got " + request.boolMap.falseValue )()
		};

		if (request.longMapArray[0].long1 == 1L) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for longMapArray, index 0, long1 failed, got " + request.longMapArray[0].long1 )()
		};

		if (request.longMapArray[0].long2 == 2L) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for longMapArray, index 0, long2 failed, got " + request.longMapArray[0].long2 )()
		};

		if (request.longMapArray[1].long1 == 3L) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for longMapArray, index 1, long1 failed, got " + request.longMapArray[1].long1 )()
		};

		if (request.longMapArray[1].long2 == 4L) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for longMapArray, index 1, long2 failed, got " + request.longMapArray[1].long2 )()
		};

		if (request.intArray[0] == 0) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intArray, index 0 failed, got " + request.intArray[0] )()
		};

		if (request.intArray[1] == 1) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intArray, index 1 failed, got " + request.intArray[1] )()
		};

		// Copy the full request and return it
		response << request
	} ] {nullProcess}
}
