import random
from random import randint

#Border Intersections
intersectionsBn = ['I-N340-04', 'I-CV1501-02', 'I-CV1501-02','I-CV149-03', 'I-CV149-03', 'I-CV149-03']
intersectionsCs = ['I-N340-01', 'I-CV1520-01', 'I-N340a-01', 'I-CV149-01', 'I-CV149-01' , 'I-CV149-01']

algorithms = ['shortest', 'fastest', 'startSmart', 'dynamicSmart']

percentNoSmart =      [0,    0,  0,  0,  0,  0,  0,  0,  0,  0,  0]
percentStartSmart =   [0,    5, 10, 15,  20, 25, 30, 35, 40, 45, 50]
percentDynamicSmart = [100, 95, 90, 85, 80, 75, 70, 65,  60,  55, 50]

data = []

def generateDatas(startinHour, finalHour, num):
	
	for x in range(num):
		
		if(randint(0,2) == 0):
			A = intersectionsCs
			B = intersectionsBn
		else:
			A = intersectionsBn
			B = intersectionsCs
		start = random.choice(A)
		end = random.choice(B)
	
		hour = randint(startinHour, finalHour)
		minute = randint(0, 59)
		
		speed = randint(85, 100)
		
		data.append([start,end,hour,minute,speed])
	

def generateRandomSample(pNoSmart, pStartSmart, pDynamicSmart):

	nameFileSmart = str(pNoSmart) + 'N'
	if(pStartSmart == pDynamicSmart):
		nameFileSmart += str(pStartSmart) + 'S'
	elif(pStartSmart > pDynamicSmart):
		nameFileSmart += str(pStartSmart) + 'SS'
	else:
		nameFileSmart += str(pDynamicSmart) + 'DS'

	nameFileSmart += ".csv"
	eventsFile = open(nameFileSmart , 'a')
	
	for x in range(len(data)):
		event = data[x]
		print(event)
		intAlgorithm = randint(0, 99)
		if(intAlgorithm < pNoSmart):
			algorithm = algorithms[0]
		elif(intAlgorithm < pNoSmart*2):
			algorithm = algorithms[1]
		elif(intAlgorithm < (pNoSmart*2) + pStartSmart):
			algorithm = algorithms[2]
		else:
			algorithm = algorithms[3]

		eventsFile.write("newCar," + str(event[2]).zfill(2) + ":" + str(event[3]).zfill(2) + "," + event[0] + "," + event[1] +
			"," + str(event[4]) + "," + algorithm + "\n")

#All day
print('Genera datos desde las 18:00 hasta las 19:00')
generateDatas(20,20,1900)
for i in range(len(percentNoSmart)):
	generateRandomSample(percentNoSmart[i], percentStartSmart[i], percentDynamicSmart[i])