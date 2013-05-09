include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

main
{
	twice@TwiceService( 5 )( response );
	print@Console(response + "\n")()
}
