include "variantInterface.iol"
include "console.iol"

inputPort VariantServer {
	Location: "dbus:/net.jolie.variant:/variantServer"
	Interfaces: Variant
}

execution { sequential }

main
{
	[ testVariant ( request ) ( response )  {
		if (request.intValue == 42) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intValue failed, got " + request.intValue )()
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

		if (request.vArray[0] == 0) {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intArray, index 0 failed, got " + request.vArray[0] )()
		};

		if (request.vArray[1] == "John") {
			println@Console( "Passed" )()
		} else {
			println@Console( "Test for intArray, index 1 failed, got " + request.vArray[1] )()
		};

		// Copy the full request and return it
		response << request
	} ] {nullProcess}
}