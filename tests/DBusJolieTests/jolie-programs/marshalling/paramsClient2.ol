include "console.iol"

// The user might very well create a type that does not match what DBus expects
type falseDbustype:void {
	.params[1, *]: undefined
	.somethingElse: undefined
}

interface Params {
	RequestResponse: 
		testParams ( falseDbustype )( void )
}

outputPort ParamsServer {
	Location: "dbus:/net.jolie.map:/paramsServer" // paramsServer is never instantiated because we expect an error before the message is sent
	Interfaces: Params
}

main 
{
	argument.params[0].field1 = 42;
	argument.params[0].field2 = "John Doe";
	argument.somethingElse = false;

	testParams@ParamsServer( argument )(  )
}