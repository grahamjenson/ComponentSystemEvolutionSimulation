package nz.geek.maori.cudf.satutils.constraints;

import java.util.HashSet;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;

public class Model {

	public Model(HashSet<Object> ins, HashSet<Object> outs) {
		super();
		this.ins = ins;
		this.outs = outs;
	}

	private HashSet<Object> ins;
	private HashSet<Object> outs;
	public HashSet<Object> getIns() {
		return ins;
	}
	public HashSet<Object> getOuts() {
		return outs;
	}

	ProfileChangeRequest pcr = null;

	public ProfileChangeRequest toPCR()
	{
		if(pcr  == null)
		{
			pcr = CUDFFactory.eINSTANCE.createProfileChangeRequest();
			for(Object o : this.getIns())
			{
				if(!(o instanceof Package)){continue;}
				Package p = (Package)o;
				Package np = CUDFFactory.eINSTANCE.createPackage();
				np.setName(p.getName());
				np.setVersion(p.getVersion());
				np.setInstalled(true);

				pcr.addPackage(np);
			}
		}
		return pcr;
	}

}
