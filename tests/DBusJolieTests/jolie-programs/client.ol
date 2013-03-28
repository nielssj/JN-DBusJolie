include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "localsocket:/tmp/mysocket.txt"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice@TwiceService( 5 )( response );
	print@Console(response)()
}
