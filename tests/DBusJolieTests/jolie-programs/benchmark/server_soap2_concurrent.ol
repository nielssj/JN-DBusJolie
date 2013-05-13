include "console.iol"
include "TwiceInterface2.iol"

inputPort TwiceService {
	Location: "socket://localhost:8888"
	Protocol: soap
	Interfaces: LargeTwiceInterface
}

execution { concurrent }

init
{
	print@Console("listening..\n")()
}

main
{
	twice ( number ) ( response ) {
		response = number.values[1] * 2
	}
}
