include "console.iol"
include "TwiceInterface.iol"

outputPort TwiceService {
	Location: "dbus:/org.testname:/object"
	Interfaces: TwiceInterface
}

main
{
	// Initialize array to all 0's
	for ( i = 0, i < 50, i++) {
	  response[i] = 0
	};

	// Do requests
	for ( i = 0, i < 50, i++) {
	  twice@TwiceService( i )( response[i] )
	};
	
	// Print array content before terminating
	print@Console("Responses: [")();
	for ( k = 0, k < 50, k++ ) {
	  print@Console(response[k] + ", ")()
	};
	print@Console("]\n")()
}
