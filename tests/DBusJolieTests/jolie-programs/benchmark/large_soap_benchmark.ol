include "console.iol"
include "TwiceInterface2.iol"

outputPort TwiceService {
	Location: "socket://localhost:8888"
	Protocol: soap
	Interfaces: LargeTwiceInterface
}

main
{
	for( i = 0, i < 50000, i++) {
		msg.values[i] = i
	};
	
	for ( j = 0, j < 1000, j++) {
		twice@TwiceService( msg )( response )
	}
}
