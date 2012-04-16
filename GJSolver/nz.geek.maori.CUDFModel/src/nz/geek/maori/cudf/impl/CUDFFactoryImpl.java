/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package nz.geek.maori.cudf.impl;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.Preamble;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Request;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class CUDFFactoryImpl implements CUDFFactory {

	private static CUDFFactoryImpl INSTANCE;

	public static CUDFFactoryImpl init() {
		if (INSTANCE == null) {
			INSTANCE = new CUDFFactoryImpl();
		}
		return INSTANCE;
	}

	/**
	 * Creates an instance of the factory. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	private CUDFFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ProfileChangeRequest createProfileChangeRequest() {
		ProfileChangeRequestImpl profileChangeRequest = new ProfileChangeRequestImpl();
		return profileChangeRequest;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Preamble createPreamble() {
		PreambleImpl preamble = new PreambleImpl();
		return preamble;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public nz.geek.maori.cudf.Package createPackage() {
		PackageImpl package_ = new PackageImpl();
		return package_;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Request createRequest() {
		RequestImpl request = new RequestImpl();
		return request;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public PackageList createPackageList() {
		PackageListImpl packageList = new PackageListImpl();
		return packageList;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public PackageFormula createPackageFormula() {
		PackageFormulaImpl packageFormula = new PackageFormulaImpl();
		return packageFormula;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public PackageVersionConstraint createPackageVersionConstraint() {
		PackageVersionConstraintImpl packageVersionConstraint = new PackageVersionConstraintImpl();
		return packageVersionConstraint;
	}

} // CUDFFactoryImpl
