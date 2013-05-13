include "complexTypesInterface.iol"
include "console.iol"

outputPort ComplexServer {
	Location: "dbus:/net.jolie.complex:/complexServer"
	Interfaces: Complex
}

main 
{
	argument.intValue = 42;
	argument.stringValue = "John Doe";
	argument.boolMap.trueValue = true;
	argument.boolMap.falseValue = false;
	argument.intArray[0] = 0;
	argument.intArray[1] = 1;
	argument.longMapArray[0].long1 = 1L;
	argument.longMapArray[0].long2 = 2L;
	argument.longMapArray[1].long1 = 3L;
	argument.longMapArray[1].long2 = 4L;

	testParams@ComplexServer( argument )( response );

	if (response.intValue == 42) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intValue failed, got " + response.intValue )()
	};

	if (response.stringValue == "John Doe") {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for stringValue failed, got " + response.stringValue)()
	};

	if (response.boolMap.trueValue == true) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for pboolMap trueValue failed, got " + response.boolMap.trueValue )()
	};

	if (response.boolMap.falseValue == false) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for boolMap falseValue failed, got " + response.boolMap.falseValue )()
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

	if (response.intArray[0] == 0) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intArray, index 0 failed, got " + response.intArray[0] )()
	};

	if (response.intArray[1] == 1) {
		println@Console( "Passed" )()
	} else {
		println@Console( "Test for intArray, index 1 failed, got " + response.intArray[1] )()
	}
}
