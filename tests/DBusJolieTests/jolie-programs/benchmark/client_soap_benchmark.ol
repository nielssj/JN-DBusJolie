include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "socket://localhost:8888"
	Protocol: soap
	Interfaces: TwiceInterface
}

main
{
	for ( i = 0, i < 1000, i++) {
		twice@TwiceService( 21 )( response );
		println@Console(i)()
	}
}
