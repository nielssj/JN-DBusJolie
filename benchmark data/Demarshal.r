soap <- read.csv("soap200.csv", header=T)
dbus <- read.csv("dbus200.csv", header=T)

attach(soap)
attach(dbus)
sortedSoap <- sort(Recieve)
sortedDbus <- sort(Demarshal)

dsd <- sd(sortedDbus)
ssd <- sd(sortedSoap)
dmean <- mean(sortedDbus)
smean <- mean(sortedSoap)
plot(sortedDbus, dnorm(sortedDbus, mean=dmean,sd=dsd), ylim=c(0,8), xlim=c(-.3,7),type="l", col="red",ann=F)
lines(sortedSoap, dnorm(sortedSoap,mean=smean,sd=ssd),type="l", col="blue")
abline(v=smean-3*ssd, col=rgb(0,0,1, 0.3), lty=3)
abline(v=smean, col=rgb(0,0,1, 0.3), lty=3)
abline(v=dmean, col=rgb(1,0,0,0.5), lty=3)
abline(v=dmean+3*dsd, col=rgb(1,0,0,0.5), lty=3)
title(main="Demarshalling 200 ints, 100.000 runs", xlab="ms")

text(-.1,8, expression("m"["D-Bus"]))
text(4.3,2, expression("m"["SOAP"]))
text(2.5,1, expression("3 SD"["SOAP"]))
text(0.6,6.9, expression("3 SD"["D-Bus"]))
arrows(0.4,7.1, x1=0.15,y1=7.4, length=.08)

dy <- 7.5
sy <- .8
arrows(smean-3*ssd, sy, x1=smean, sy, code=3, angle=24, length=0.1, col=rgb(0,0,1, 0.6))
arrows(dmean, dy, x1=dmean+3*dsd, dy, code=3, angle=24, length=0.06, col=rgb(1,0,0, 0.6))

legend(4.3,7.9, 
  c(
    paste("D-Bus, Mean: ",format(dmean, digits=2), " SD: ", format(dsd, digits=2)),
    paste("SOAP, Mean: ",format(smean, digits=4)," SD: ",format(ssd,  digits=3))
  ), col=c("red","blue"), pch="l", cex=0.9)

