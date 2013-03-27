include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "localsocket:me/soeholm/csexample"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice@TwiceService( 5 )( response );
	println@Console( response )()
}
