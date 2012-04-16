import apt_pkg #useful version_compare parse_depends
apt_pkg.init()

_package = "Package: "
_version = "Version: "
_depends = "Depends: " 
_pre_depends= "Pre-Depends: "
_provides = "Provides: "
_recommends = "Recommends: "
_suggests = "Suggests: "
_enhances = "Enhances: "
_breaks = "Breaks: "
_conflicts = "Conflicts: "

_priority = "Priority: "
_description = "Description: "
_section = "Section: "
_installed_size = "Installed-Size: "
_maintainer = "Maintainer: "
_source = "Source: "
_architecture = "Architecture: "
_original_maintainer = "Original-Maintainer: "
_replaces = "Replaces: "
_homepage = "Homepage: "

class DebianPackage:
	def __init__(self,lines) :
		self.version = ""
		self.name = ""
		self.recommends = []
		self.suggests = []
		self.pre_depends = []
		self.depends = []
		self.conflicts = []
		self.provides = []
		self.arch = ""
		self.priority = ""
		for line in lines:
			if line.startswith(" " or "\t") or line.strip() == "" :
					None
			elif line.startswith(_package):
				self.name = line[len(_package):].strip()
			elif line.startswith(_version):
				self.version = line[len(_version):].strip()
			elif line.startswith(_depends):
				self.depends = apt_pkg.parse_depends(line[len(_depends):].strip())
			elif line.startswith(_pre_depends):
				self.pre_depends = apt_pkg.parse_depends(line[len(_pre_depends):].strip())
			elif line.startswith(_provides):
				self.provides = apt_pkg.parse_depends(line[len(_provides):].strip())
			elif line.startswith(_recommends):
				self.recommends = apt_pkg.parse_depends(line[len(_recommends):].strip())
			elif line.startswith(_suggests):
				self.suggests = apt_pkg.parse_depends(line[len(_suggests):].strip())
			elif line.startswith(_enhances):
				None
			elif line.startswith(_breaks):
				None
			elif line.startswith(_conflicts):
				self.conflicts = apt_pkg.parse_depends(line[len(_conflicts):].strip())
			elif line.startswith(_priority):
				self.priority = line[len(_package):].strip()
			elif line.startswith(_description):
				None
			elif line.startswith(_section):
				None
			elif line.startswith(_installed_size):
				None
			elif line.startswith(_maintainer):
				None
			elif line.startswith(_source):
				None
			elif line.startswith(_architecture):
				self.arch = line[len(_architecture):].strip()
			elif line.startswith(_original_maintainer):
				None
			elif line.startswith(_replaces):
				None
			elif line.startswith(_homepage):
				None
		else : None

	def __str__(self):
		sb = ""
		sb += self.name 
		sb += "\n"
		sb += self.version 
		return sb
