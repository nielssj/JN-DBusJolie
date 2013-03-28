include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "localsocket:/tmp/mysocket.txt"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
