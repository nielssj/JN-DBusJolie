include "TwiceInterface.iol"

inputPort TwiceService {
	Location: "localsocket:/home/niels/Jolie/mysocket2.txt"
	Protocol: sodep
	Interfaces: TwiceInterface
}

main
{
	twice ( number ) ( response ) {
		response = number * 2
	}
}
