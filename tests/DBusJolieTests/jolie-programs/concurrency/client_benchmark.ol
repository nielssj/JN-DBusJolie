include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

main
{
	for ( i = 0, i < 1000, i++) {
		twice@TwiceService( 21 )( response )
	}
}
