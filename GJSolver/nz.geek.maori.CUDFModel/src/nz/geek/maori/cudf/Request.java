package nz.geek.maori.cudf;

public interface Request {
	String getName();

	void setName(String value);

	PackageList getInstall();

	void setInstall(PackageList value);

	PackageList getRemove();

	void setRemove(PackageList value);

	PackageList getUpgrade();

	void setUpgrade(PackageList value);

	String toCUDF();
} // Request
