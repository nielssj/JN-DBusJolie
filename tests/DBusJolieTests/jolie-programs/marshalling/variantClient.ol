include "variantInterface.iol"
include "console.iol"

outputPort VariantServer {
	Location: "dbus:/net.jolie.variant:/variantServer"
	Interfaces: Variant
}

main 
{
	argument.intValue = 42;
	argument.vArray[0] = 0;
	argument.vArray[1] = "John";
	argument.longMapArray[0].long1 = 1L;
	argument.longMapArray[0].long2 = 2L;	
	argument.longMapArray[1].long1 = 3L;
	argument.longMapArray[1].long2 = 4L;

	testVariant@VariantServer( argument )( response );

	if (response.intValue == 42) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intValue failed, got " + response.intValue )()
	};

	if (response.longMapArray[0].long1 == 1L) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for longMapArray, index 0, long1 failed, got " + response.longMapArray[0].long1 )()
	};

	if (response.longMapArray[0].long2 == 2L) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for longMapArray, index 0, long2 failed, got " + response.longMapArray[0].long2 )()
	};

	if (response.longMapArray[1].long1 == 3L) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for longMapArray, index 1, long1 failed, got " + response.longMapArray[1].long1 )()
	};

	if (response.longMapArray[1].long2 == 4L) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for longMapArray, index 1, long2 failed, got " + response.longMapArray[1].long2 )()
	};

	if (response.vArray[0] == 0) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for vArray, index 0 failed, got " + response.vArray[0] )()
	};

	if (response.vArray[1] == "John") {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for vArray, index 1 failed, got " + response.vArray[1] )()
	}
}