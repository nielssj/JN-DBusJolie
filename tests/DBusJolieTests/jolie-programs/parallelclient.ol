include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

main
{
	twice@TwiceService( 5 )( response ) | twice@TwiceService( 7 )( response2 );
	
	print@Console(response)();
	print@Console(response2)()
}
