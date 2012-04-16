package nz.geek.maori.cudf;

public interface CUDFFactory {
	CUDFFactory eINSTANCE = nz.geek.maori.cudf.impl.CUDFFactoryImpl.init();

	ProfileChangeRequest createProfileChangeRequest();

	Preamble createPreamble();

	Package createPackage();

	Request createRequest();

	PackageList createPackageList();

	PackageFormula createPackageFormula();

	PackageVersionConstraint createPackageVersionConstraint();

} // CUDFFactory
