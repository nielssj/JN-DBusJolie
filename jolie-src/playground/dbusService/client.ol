include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/me/soeholm/coolbus"
	Interfaces: TwiceInterface
}

main
{
	twice@TwiceService( 5 )( response );
	println@Console( "Joe" )()
}
