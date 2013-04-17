include "paramsInterface.iol"
include "console.iol"

type dbustype:void {
	.params[1, *]: undefined
}


interface Map {
	RequestResponse: 
		testMap ( dbustype )( dbustype )
}

outputPort MapServer {
	Location: "dbus:/net.jolie.map:/mapServer" // MapServer is never instantiated because we expect an error before the message is sent
	Interfaces: Map
}

main 
{
	argument.params[0].field1 = 42;
	argument.params[0].field2 = "John Doe";

	testMap@MapServer( argument )( response )
}