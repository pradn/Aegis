import re
from Tkinter import *
import tkFont
import string
import optparse as opt
import os
import math

################### ERROR HANDLING ###################

def exit(message):
	dialogue('Fatal Error', 'red', message)
	sys.exit(message)

def warn(message):
	dialogue('Warning', 'black', message)

def dialogue(title, fontcolor, message):
	root = Tk()
	root.title(title)
	xpos = int(root.winfo_screenwidth() / 2) - 100
	ypos = int(root.winfo_screenheight() / 2) - 100
	root.geometry('-%s+%s' % (xpos, ypos))
	text = Label(root, fg=fontcolor)
	text.config(text=message)
	text.pack(anchor=W, pady=1, padx=3)
	root.mainloop()

####################### CLASSES #######################

class tester:
	def __init__(self, dnaid):
		# setup fields
		self.dna = getdna(dnaid)
		self.dnaid = dnaid
		self.root = Tk()
		self.root.title('Testing DNA \'%d\'' % dnaid)
		self.stringvars = []
		self.sigmoidvar = StringVar(self.root)
		self.outputvars = []
		# create sorted nodes list
		self.nodes = []
		for key in sorted(self.dna.nodes.keys()):
			self.nodes.append(self.dna.nodes[key])
		# create GUI
		Button(self.root, text='Pump', command=self.pump).pack()
		Label(self.root, text='Sigmoid Coefficient:').pack()
		Entry(self.root, width=15, textvariable=self.sigmoidvar).pack()
		i = 0
		for node in self.dna.inputs:
			Label(self.root, text='Input %d:' % node.id).pack()
			stringvar = StringVar(self.root)
			Entry(self.root, width=15, textvariable=stringvar).pack()
			self.stringvars.append(stringvar)
			i += 1
		i = 0
		for node in self.nodes:
			stringvar = StringVar(self.root)
			self.outputvars.append(stringvar)
			stringvar.set('%f' % node.output)
			if node.type == 1:
				Label(self.root, fg='red', text='Node %d Output:' % node.id).pack()
			elif node.type == 2:
				Label(self.root, fg='blue', text='Node %d Output:' % node.id).pack()
			else:
				Label(self.root, fg='green', text='Node %d Output:' % node.id).pack()
			Label(self.root, textvariable=self.outputvars[i]).pack()
			i += 1
		Button(self.root, text='Clear', command=self.clear).pack()
		self.root.mainloop()

	def clear(self):
		i = 0
		for node in self.nodes:
			node.output = 0
			node.activity = 0
			self.outputvars[i].set('%f' % node.output)
			i += 1

	def pump(self):
		i = 0
		for stringvar in self.stringvars:
			try:
				self.dna.inputs[i].activity = float(stringvar.get().strip())
			except ValueError:
				warn('Invalid value specified in the inputs.')
				return
			i += 1
		for node in self.dna.hiddens:
			self.updateActivity(node)
		for node in self.dna.outputs:
			self.updateActivity(node)
		i = 0
		for node in self.nodes:
			node.output = self.sigmoid(node.activity)
			if node.type != 1:
				node.activity = 0.0
			self.outputvars[i].set('%f' % node.output)
			i += 1

	def sigmoid(self, val):
		try:
			sigmoidCoefficient = float(self.sigmoidvar.get())
		except ValueError:
			warn('Invalid value specified for the sigmoid coefficient.')
			return
		sigmoid = float(1) / (1 + (math.e ** (val * sigmoidCoefficient)))
		return sigmoid

	def updateActivity(self, node):
		sum = 0.0
		for gene in self.dna.genes.values():
			if gene.end == node.id and gene.enabled == 1:
				sum += gene.weight * self.dna.getnode(gene.start).output
		node.activity = sum

class editor:
	def __init__(self, dnaid):
		self.dna = getdna(dnaid)
		self.dnaid = dnaid
		self.root = Tk()
		self.root.title('Editing DNA \'%d\'' % dnaid)
		Button(self.root, text='Update', command=self.update).pack(side=TOP)
		# make textbox
		vertscrollbar = Scrollbar(self.root)
		self.textbox = Text(self.root, width=40)
		vertscrollbar.pack(side=RIGHT, fill=Y)
		self.textbox.pack(side=RIGHT, fill=X)
		vertscrollbar.config(command=self.textbox.yview)
		self.textbox.config(yscrollcommand=vertscrollbar.set)
		# insert text
		self.textbox.insert(END, self.dna.text)
		self.root.mainloop()
#	genebox.delete(1.0, END)
#	genebox.insert(END, dna.text)

	def update(self):	
		dna = self.dna
		dna.text = self.textbox.get(1.0, END)[:-1]
		dna.text2fields()
		updatedna(self.dnaid)

class dna:
	def __init__(self, textdata):
		txt = re.search('genomestart (\d+)\n(.*)\n', textdata, re.S)
		self.id = int(txt.expand(r'\1'))
		self.text = txt.expand(r'\2')
		self.linenum2obj = {}
		self.par1 = 0
		self.par2 = 0
		self.mutations = []
		self.nodes = {}
		self.genes = {}
		self.children = []
		self.inputs = []
		self.outputs = []
		self.hiddens = []
		self.text2fields()

	def getnode(self, id):
		return self.nodes[str(id)]

	def getgene(self, id):
		return self.genes[str(id)]

	def text2fields(self):
		self.nodes = {}
		self.genes = {}
		del self.inputs[:]
		del self.outputs[:]
		del self.hiddens[:]
		self.linenum2obj = {}
		allnodes = re.findall('node (\d+) (\d+)', self.text)
		allgenes = re.findall('gene (\d+) (\d+) (\d+) (.+) (\d)', self.text)

		i = 1
		for nodeinfo in allnodes:
			nodeid = nodeinfo[0]
			nodetype = nodeinfo[1]
			newnode = node(nodeid, nodetype)
			self.nodes[nodeinfo[0]] = newnode
			self.linenum2obj[i] = newnode
			if newnode.type == 1:
				self.inputs.append(newnode)
			elif newnode.type == 2:
				self.outputs.append(newnode)
			else:
				self.hiddens.append(newnode)
			i += 1

		for geneinfo in allgenes:
			geneinno = geneinfo[0]
			genestart = geneinfo[1]
			geneend = geneinfo[2]
			geneweight = geneinfo[3]
			geneenabled = geneinfo[4]
			newgene = gene(geneinno, genestart, geneend, geneweight, geneenabled)
			self.genes[geneinfo[0]] = newgene
			self.linenum2obj[i] = newgene
			i += 1

class node:
	def __init__(self, id, type):
		self.id = int(id)
		self.type = int(type)
		self.output = 0.0
		self.activity = 0.0
		self.centerx = 0
		self.centery = 0
		self.x1 = 0
		self.y1 = 0
		self.x2 = 0
		self.y2 = 0

class gene:
	def __init__(self, innovation, start, end, weight, enabled):
		self.innovation = int(innovation)
		self.start = int(start)
		self.end = int(end)
		self.weight = float(weight)
		self.enabled = int(enabled)
		self.centerx = 0
		self.centery = 0
		self.x1 = 0
		self.y1 = 0
		self.x2 = 0
		self.y2 = 0

class mutation:
	def __init__(self, type, dna, **kwargs):
		self.type = type
		self.dna = dna

############### BUTTON PRESS METHODS #################

def on_craft_press():
	dnaid = getdnafieldvalue()
	editor(dnaid)

def on_test_press():
	dnaid = getdnafieldvalue()
	tester(dnaid)

def on_mouse_click(event):
	dna = getdna(getdnafieldvalue())
	updatedna(getdnafieldvalue())
	linenum = 3
	#linenum = string.split(genebox.index(CURRENT), ".")[0]
	drawbox(dna.linenum2obj[int(linenum)], 'hey')

def on_view_press():
	dnaid = getdnafieldvalue()
	updatedna(dnaid)

def on_par1_press():
	if par1stringvar.get() == 'N/A':
		return
	updatedna(par1stringvar.get())

def on_par2_press():
	if par2stringvar.get() == 'N/A':
		return
	updatedna(par2stringvar.get())

def on_first_press():
	updatedna(1)

def on_last_press():
	updatedna(len(alldna))

def on_left_press():
	dnaid = getdnafieldvalue() - 1
	if dnaid < 1:
		return
	updatedna(dnaid)

def on_right_press():
	dnaid = getdnafieldvalue() + 1
	if dnaid > len(alldna):
		return
	updatedna(dnaid)

def on_back_press():
	if len(backstack) < 2:
		return
	forstack.append(backstack.pop())
	entrystringvar.set(backstack[len(backstack) - 1])
	refreshdrawing(getdna(backstack[len(backstack) - 1]))

def on_for_press():
	if len(forstack) == 0:
		return
	returnedto = forstack.pop()
	backstack.append(returnedto)
	entrystringvar.set(returnedto)
	refreshdrawing(getdna(returnedto))

def on_clear_press():
	global backstack
	if len(backstack) > 0:
		save = backstack.pop()
	backstack = [save]
	del forstack[:]

################## GUI METHODS ########################

def updatedna(id):
	# Check ID's validity
	id = int(id)
	if id < 1 or id > len(alldna) or id == 0:
		warn('Not a valid DNA ID')
		return
	
	# Clear forstack
	if len(forstack) > 0:
		del forstack[:]
	
	# Edit fields
	entrystringvar.set(id)
	backstack.append(id)
	
	# Refresh drawing
	dna = getdna(id)
	refreshdrawing(dna)

def refreshdrawing(dna):
	# Update navigation
	updatenav()
	
	# Update main canvas
	maincanvas.delete(ALL)
	drawnetwork(maincanvas, dna, True)
	
	# Update parent 1
	par1canvas.delete(ALL)
	if dna.par1 != 0:
		drawnetwork(par1canvas, getdna(dna.par1), True)

	# Update parent 2
	par2canvas.delete(ALL)
	if dna.par2 != 0:
		drawnetwork(par2canvas, getdna(dna.par2), True)

def updatenav():
	global childrenmenu
	dnaid = getdnafieldvalue()
	dna = getdna(dnaid)

	# Update ID label
	idlabel['text'] = 'ID: %s' % dna.id

	# Update parents
	set1 = dna.par1
	set2 = dna.par2
	if dna.par1 == 0:
		set1 = 'N/A'
	if dna.par2 == 0:
		set2 = 'N/A'

	par1stringvar.set(set1)
	par2stringvar.set(set2)

def calculatepositions(canvas, dna):
	configwidth = canvas.config()['width']
	configheight = canvas.config()['height']
	canvaswidth = int(configwidth[len(configwidth) - 1])
	canvasheight = int(configheight[len(configheight) - 1])

	circlediameter = int(canvaswidth / max((len(dna.inputs), len(dna.outputs), len(dna.hiddens)))) - 2
	if circlediameter > 25:
		circlediameter = 25

	inputy = int(canvasheight * 0.9)
	hiddeny = int(canvasheight * .5)
	outputy = int(canvasheight * .1)
	
	xinc = int(canvaswidth / (len(dna.inputs) + 1)) - int(circlediameter / 2)
	x = xinc
	for node in dna.inputs:
		node.centerx = x + int(circlediameter / 2)
		node.centery = inputy + int(circlediameter / 2)
		node.x1 = x
		node.y1 = inputy
		node.x2 = x + circlediameter
		node.y2 = inputy + circlediameter
		x += xinc

	xinc = int(canvaswidth / (len(dna.hiddens) + 1)) - int(circlediameter / 2)
	x = xinc
	for node in dna.hiddens:	
		node.centerx = x + int(circlediameter / 2)
		node.centery = hiddeny + int(circlediameter / 2)
		node.x1 = x
		node.y1 = hiddeny
		node.x2 = x + circlediameter
		node.y2 = hiddeny + circlediameter
		x += xinc

	xinc = int(canvaswidth / (len(dna.outputs) + 1)) - int(circlediameter / 2)
	x = xinc
	for node in dna.outputs:
		node.centerx = x + int(circlediameter / 2)
		node.centery = outputy + int(circlediameter / 2)
		node.x1 = x
		node.y1 = outputy
		node.x2 = x + circlediameter
		node.y2 = outputy + circlediameter
		x += xinc
		
	for gene in dna.genes.values():
		if gene.start == gene.end:
			x = dna.getnode(gene.start).centerx
			y = dna.getnode(gene.start).centery
			gene.x1 = x - circlediameter
			gene.y1 = y - circlediameter
			gene.x2 = x
			gene.y2 = y
		else:
			gene.x1 = dna.getnode(gene.start).centerx
			gene.y1 = dna.getnode(gene.start).centery
			gene.x2 = dna.getnode(gene.end).centerx
			gene.y2 = dna.getnode(gene.end).centery
		gene.centerx = int(abs(gene.x1 - gene.x2)/2)
		gene.centery = int(abs(gene.y1 - gene.y2)/2)

def drawnetwork(canvas, dna, recalculate):
	if recalculate:
		calculatepositions(canvas, dna)

	# Draw nodes
	for node in dna.inputs:
		canvas.create_oval(node.x1, node.y1, node.x2, node.y2, fill="red")

	for node in dna.hiddens:
		canvas.create_oval(node.x1, node.y1, node.x2, node.y2, fill="green")

	for node in dna.outputs:
		canvas.create_oval(node.x1, node.y1, node.x2, node.y2, fill="blue")

	# Draw genes
	for gene in dna.genes.values():
		if gene.start == gene.end:
			if gene.enabled == 1:
				canvas.create_oval(gene.x1, gene.y1, gene.x2, gene.y2, fill="", outline="black")
			else:
				canvas.create_oval(gene.x1, gene.y1, gene.x2, gene.y2, fill="", outline="grey")
		else:
			if gene.enabled == 1:
				canvas.create_line(gene.x1, gene.y1, gene.x2, gene.y2, fill="black")
			else:
				canvas.create_line(gene.x1, gene.y1, gene.x2, gene.y2, fill="grey")
			
def highlightgene(gene):
	drawnetwork(maincanvas, getdna(entrystringvar.get()), False)
	if gene.start == gene.end:
		maincanvas.create_oval(gene.x1, gene.y1, gene.x2, gene.y2, fill="", outline="yellow")
	else:
		maincanvas.create_line(gene.x1, gene.y1, gene.x2, gene.y2, fill="yellow")
	drawgenebox(gene)

def highlightnode(node):
	drawnetwork(maincanvas, getdna(entrystringvar.get()), False)
	maincanvas.create_oval(node.x1, node.y1, node.x2, node.y2, fill="yellow")
	drawnodebox(node)
	
def highlightmutation(mutation):
	return

def drawgenebox(gene):
	text = 'innovation: %s\nstart: %s\nend: %s\nweight: %s\n' % (gene.innovation, gene.start, gene.end, round(gene.weight,3))
	if gene.enabled:
		text += 'enabled'
	else:
		text += 'disabled'
	drawbox(gene, text)

def drawnodebox(node):
	text = 'id: %s\n' % node.id
	if node.type == 1:
		text += 'input'
	elif node.type == 2:
		text += 'hidden'
	elif node.type == 3:
		text += 'output'
	else:
		text += 'dummy'
	drawbox(node, text)

def drawbox(element, text):
	font = tkFont.Font(size=8)
	width = 0
	textlines = text.split('\n')
	for line in textlines:
		w = font.measure(line)
		if w > width:
			width = w
	
	height = font.metrics('linespace') * len(textlines)
	x = element.centerx
	y = element.centery
	maincanvas.create_rectangle(x - 1, y - 1, x + width + 1, y + height, fill='yellow')
	maincanvas.create_text(x, y, anchor=NW, text=text, font=font)

######################## HELPERS ###########################

def getdna(id):
	return alldna[int(id) - 1]

def getdnafieldvalue():
	value = entrywidget.get().strip()
	if value == '':
		return 0
	else:
		return int(value)

def buildfromlog(filepath):
	log = open(filepath, 'r')
	logtext = log.read()
	log.close()

	logtext = logtext.replace('\r', '')
	dnalist = re.findall('(genomestart \d+\n.*?)(?=genomeend)', logtext, re.S)
	if not dnalist:
		exit('Invalid log file')

	genepool = []
	for dnainfo in dnalist:
		genepool.append(dna(dnainfo))

	# parse ancestral information
	ancestry = re.findall('(reproduction \d+ \d+ \d+\n)', logtext, re.S)
	for reproduction in ancestry:
		family = re.search('reproduction (\d+) (\d+) (\d+)\n', reproduction, re.S)
		par1 = int(family.expand(r'\1'))
		par2 = int(family.expand(r'\2'))
		gid = int(family.expand(r'\3'))
		genepool[gid-1].par1 = par1
		genepool[gid-1].par2 = par2

	return genepool
	

#################### SCRIPT #######################

# Deal with command line stuff
usage = 'This is a GUI application for visualizing logs produced by Braincraft.\n\nIt takes one argument, the location of the statistics log file generated by Braincraft.\nThis logfile can be generated by setting Braincraft.gatherStats to true before running evolution.'
parser = opt.OptionParser(usage=usage)
parser.add_option('-f', '-s', '--stats', action='store', type='string', dest='filepath', help='Path of the stats text file', metavar='Filepath')
(options, args) = parser.parse_args()

filepath = ''
if options.filepath == None:
	if len(args) == 1:
		filepath = args[0]
	else:
		exit('Stats file not specified. Aborting.')
else:
	filepath = options.filepath

# Create DNA database
#alldna = buildfromlog('/home/riboflavin/workspace/braincraft/stats.txt')
alldna = buildfromlog(filepath)

# Initialize Tkinter, constants
root = Tk()
root.title('Visualizing \'%s\'' % os.path.basename(filepath))
xpos = int(root.winfo_screenwidth() / 2) - 100
ypos = int(root.winfo_screenheight() / 2) - 100
root.geometry('-%s+%s' % (xpos, ypos))

canvaswidth = 400
canvasheight = 400
parentwidth = 200
parentheight = 200

backstack = []
forstack = []

####################### GUI ########################

#### TOP PANEL ####

# Create frame
entryframe = Frame(root)

# Add static text
Label(entryframe, text='DNA Number (1-%s):' % (len(alldna))).pack(side=LEFT)

# Add ID text field
entrystringvar = StringVar()
entrywidget = Entry(entryframe, width=5, textvariable=entrystringvar)
entrystringvar.set('1')
entrywidget.pack(side=LEFT)

# Add buttons
Button(entryframe, text="View", command=on_view_press).pack(side=LEFT)
Button(entryframe, text='1', command=on_first_press).pack(side=LEFT)
Button(entryframe, text='<<', command=on_left_press).pack(side=LEFT)
Button(entryframe, text='>>', command=on_right_press).pack(side=LEFT)
Button(entryframe, text='%s' % (len(alldna)), command=on_last_press).pack(side=LEFT)

#### CONTROL PANEL ####

# Create frame
controlpanel = Frame(root, relief=GROOVE)

# Make ID label
idlabel = Label(controlpanel, text='ID: ', font=("Helvetica", 14))
idlabel.pack(side=TOP)

# Make parent buttons
Label(controlpanel, text='Parent 1:').pack(side=TOP)
par1stringvar = StringVar()
Button(controlpanel, text='', command=on_par1_press, textvariable=par1stringvar, width=9).pack(side=TOP)
Label(controlpanel, text='Parent 2:').pack(side=TOP)
par2stringvar = StringVar()
Button(controlpanel, text='', command=on_par2_press, textvariable=par2stringvar, width=9).pack(side=TOP)

#### VISUALIZER PANEL ####
maincanvas = Canvas(root, width=canvaswidth, height=canvasheight)
par1canvas = Canvas(root, width=parentwidth, height=parentheight)
par2canvas = Canvas(root, width=parentwidth, height=parentheight)

#### GENE/NODE/MUTATION LISTS ####

# Create frame
listframe = Frame(root, relief=GROOVE)
listframe.bind('<Button-1>', on_mouse_click)

# Create update button
Button(listframe, text='Craft', command=on_craft_press).pack(side=TOP)
Button(listframe, text='Test', command=on_test_press).pack(side=TOP)

#### MORE NAV BUTTONS ####
morenav = Frame(root)
Button(morenav, text='BACKWARD', command=on_back_press).pack(side=LEFT)
Button(morenav, text='FORWARD', command=on_for_press).pack(side=LEFT)
Button(morenav, text='CLEAR HISTORY', command=on_clear_press).pack(side=LEFT)

#### PACK HIGH LEVEL CONTAINERS ####
on_view_press()
entryframe.pack()
controlpanel.pack(side=LEFT)
maincanvas.pack()
#par1canvas.pack(side=RIGHT)
#par2canvas.pack(side=RIGHT)
listframe.pack(side=RIGHT)
morenav.pack(side=BOTTOM)

#### LOOP ####
root.mainloop()
