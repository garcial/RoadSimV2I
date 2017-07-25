import random
from random import randint

intersectionsBn = ['I-CV1501-01', 'I-CV1501-02', 'I-CV149-03', 'I-N340-03', 'I-N340-04']
intersectionsCs = ['I-N340-01','I-CV1520-01', 'I-N340a-01',  'I-CV149-01']

algorithms = ['dynamicSmart', 'shortest', 'smartest', 'startSmart']
justDynamics =['dynamicSmart']
algorithmsTwo = ['dynamicSmart', 'startSmart']

eventsFile = open("eventos.csv", 'a')

def generateRandomSample(startinHour, finalHour, num, algorithmType=None):

	for x in range(num):

		if(randint(0,2) == 0):
 			A = intersectionsCs
 			B = intersectionsBn
		else:
			A = intersectionsBn
			B = intersectionsCs
		start = random.choice(A)
		end = random.choice(B)

		while (start == end):
			end = random.choice(B)

		hour = randint(startinHour, finalHour)
		minute = randint(0, 59)

		speed = randint(70, 100)

		if algorithmType == None:

			algorithm = random.choice(algorithmsTwo)
		else:
			algorithm = algorithmType

		eventsFile.write("newCar," + str(hour).zfill(2) + ":" + str(minute).zfill(2) + "," + start + "," + end +
			"," + str(speed) + "," + algorithm + "\n")

def generateStress(hour, minute, num):

		for x in xrange(num):

			start = random.choice(intersections)
			end = random.choice(intersections)

			while (start == end):
				end = random.choice(intersections)

			speed = randint(80, 120)

			eventsFile.write("newCar," + str(hour).zfill(2) + ":" + str(minute).zfill(2) + "," + start + "," + end +
				"," + str(speed) + "," + random.choice(algorithms) + "\n")

#All day
print('Genera datos desde las 19:00 hasta las 21:00')
generateRandomSample(19, 19, 1500)

#Morning
#generateRandomSample(8, 9, 1000)

#Lunch time
#generateRandomSample(13, 15, 1000)

#Evening
#generateRandomSample(16, 19, 1000)

#Night
#generateRandomSample(20, 22, 1000)

#Smart cars
#generateRandomSample(8, 23, 1000, 'smartest')
