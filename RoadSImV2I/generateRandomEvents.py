import random
from random import randint

intersections = ['I-CV10-01', 'I-CV10-02', 'I-CV10-03', 'I-CV10-04', 'I-CV10-05', 'I-CV10-06',
'I-CV10-07', 'I-CV10-08', 'I-CV10-09', 'I-CV10-10', 'I-N340-01', 'I-N340-02-AP7-02', 'I-N340-03', 'I-N340-04', 'I-N340-05',
'I-N340-06', 'I-N340-07', 'I-N340-08', 'I-N340-09', 'I-CS22-01', 'I-CS22-02', 'I-CS22-03', 'I-CS22-04',
'I-AP7-01', 'I-AP7-03', 'I-AP7-04']

algorithms = ['shortest', 'fastest', 'smartest']

eventsFile = open("events.csv", 'a')

def generateRandomSample(startinHour, finalHour, num, algorithmType=None):

	for x in xrange(num):

		start = random.choice(intersections)
		end = random.choice(intersections)

		while (start == end):
			end = random.choice(intersections)

		hour = randint(startinHour, finalHour)
		minute = randint(0, 59)

		speed = randint(80, 120)

		if algorithmType == None:

			algorithm = random.choice(algorithms)
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
generateRandomSample(8, 23, 5000)

#Morning
generateRandomSample(8, 9, 1000)

#Lunch time
generateRandomSample(13, 15, 1000)

#Evening
generateRandomSample(16, 19, 1000)

#Night
generateRandomSample(20, 22, 1000)

#Smart cars
generateRandomSample(8, 23, 1000, 'smartest')
