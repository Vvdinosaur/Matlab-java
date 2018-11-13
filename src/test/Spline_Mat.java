package test;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.matrix.factory.SparseMatrixFactory;
import org.ujmp.core.util.MathUtil;



public class Spline_Mat{
	
//	public static void main(String[] args) {
//		double[] x = { 1,8,15,23,31,39,47,55,63,71,79,87,94,102,110,118,126,134,142,149,157,165,173,181,188,196,204,212,220,227,235 };
//		double[] y = {1.34026,1.3402,1.2658,1.2500,1.2822,1.2658,1.2658,1.2500,1.2822,1.2658,1.2658,1.2658,1.2658,1.28205,1.2658,1.2658,1.2987,1.2658,1.2658,1.2658,1.2822,1.2822,1.2987,1.2822,1.2987,1.2500,1.2821,1.2821,1.2821,1.2821,1.2658};
//		double[] xx = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
//		
//		//double[] x = { 1,8,15,23,31,39};
//		//double[] y = {1.34026,1.3402,1.2658,1.2500,1.2822,1.2658};
//		
//		try {
//			double[] endslopes = spline(x, y, xx);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//	}
	

	public static double[] spline(double[] x, double[] y, double[] xx) throws Exception{
		// TODO Auto-generated method stub
		double[] v = new double[xx.length];
		int n = x.length;
		if(n<2){
			throw new Exception("chckxy:Not Enough Points");
		}else{
			// re-sort, if needed, to ensure strictly increasing site sequence:
			double[] dx = diff(x);
			int[] ind = new int[n];
			if (anyLower(dx, 0.0)){
				x = sort(x, 0, n-1);
				for(int j=0; j<x.length; j++){
					System.out.println(x[j]);
				}
				dx = diff(x);
				//���䷵�������ԭʼԪ���±�ĺ���
			}else{
				for(int j=0; j<x.length; j++){
					ind[j] = j+1;
				}								
			}
			
			double[] dy = diff(y);
			double[] divdif = new double[dx.length];
			for(int j=0; j<dx.length; j++){
				divdif[j] = dy[j]/dx[j];
				//System.out.println(divdif[j]);
			}
			double[] b = new double[n];
			for(int j = 0; j<n; j++){
				b[j] = 0;
			}
			for(int j = 1; j<n-1; j++){
				b[j] = 3*(dx[j]*divdif[j-1]+dx[j-1]*divdif[j]);
				//System.out.println(b[j]);
			}
			double x31 = x[2]-x[0];
			double xn = x[n-1] - x[n-3];
			b[0] = ((dx[0]+2*x31)*dx[1]*divdif[0]+dx[0]*dx[0]*divdif[1])/x31;
			b[n-1] = (dx[n-2]*dx[n-2]*divdif[n-3]+(2*xn+dx[n-2])*dx[n-3]*divdif[n-2])/xn;
			
			double[] dxt = dx;
			double[] a1 = new double[n-1];
			double[] a2 = new double[n];
			double[] a3 = new double[n-1];
			a1[0] = x31;
			a2[0] = dxt[1];
			a2[n-1] = dxt[n-3];
			a3[n-2] = xn;
			for(int j =1; j<n-1; j++){
				a1[j] = dxt[j-1];
				a2[j] = 2*(dxt[j]+dxt[j-1]);
				a3[j-1] = dxt[j];
			}
			//double[][] c = spdiags(a1, a2, a3, n);
			//Ϊϡ�����ֵ
			SparseMatrix c = SparseMatrix.Factory.zeros(n, n);
			for(int j = 0; j<n-1; j++){
				c.setAsDouble(a2[j], j, j);
				c.setAsDouble(a1[j], j+1, j);
				c.setAsDouble(a3[j], j, j+1);
			}
			c.setAsDouble(a2[n-1], n-1, n-1);
			//Matrix bb = Matrix.Factory.importFromArray(b);
			//System.out.println(bb.getColumnCount());
			// �Ż������þ���ĳ�������
			double[] s = getMatrixDivide(b, c);
			//System.out.println(s);
			double[] dxd = dx;
			double[] dzzdx = new double[n-1];
			double[] dzdxdx = new double[n-1];
			for(int j =0; j<n-1; j++){
				dzzdx[j] = (divdif[j]-s[j])/dxd[j];
				dzdxdx[j] = (s[j+1]-divdif[j])/dxd[j];
				//System.out.println(dzzdx[j]);
				//System.out.println(dzdxdx[j]);
			}
			//�������þ�����ʽ
			double[][] coef = new double[n-1][4];
			for(int j = 0; j<n-1; j++){
				coef[j][0] = (dzdxdx[j] - dzzdx[j])/dxd[j];
				coef[j][1] = 2*dzzdx[j] - dzdxdx[j];
				coef[j][2] = s[j];
				coef[j][3] = y[j];
			}
			
			// calculate index
			
			double[] xs = new double[xx.length];
			int[] index = new int[xx.length];
			
			for(int i = 0; i<xx.length; i++){
				for(int j = 0; j<x.length-1; j++){
					//System.out.println(x[j]);
					if(xx[i]>=x[j] & xx[i]<x[j+1]){
						index[i] = j;
						v[i] = coef[index[i]][0];
						xs[i] = xx[i] - x[index[i]];
						//System.out.println(v[i]);
						break;
						
					}
				}
			}
			
			for(int i = 1; i<4; i++){
				for(int j = 0; j<xx.length; j++){
					v[j] = xs[j]*v[j] + coef[index[j]][i]; 
				}
			}
//			for(int i = 0; i<xx.length; i++){
//				System.out.println(v[i]);
//			}
									
			//�ж�y�Ƿ����Ҫ��
			if(x.length != y.length){
				throw new Exception("chckxy:x and y are not the same length!");
			}else{
				
			}
			
		}
		return v;
	}





	private static int[] calcute_histc(double[] x, double[] xx) {
		// TODO Auto-generated method stub
		int[] index = new int[xx.length];
		for(int i = 0; i<xx.length; i++){
			for(int j = 0; j<x.length-1; j++){
				//System.out.println(x[j]);
				if(xx[i]>=x[j] & xx[i]<x[j+1]){
					index[i] = j;
					//System.out.println(index[i]);
					break;
					
				}
			}
		}
		return index;
	}

	private static double[] getMatrixDivide(double[] b, SparseMatrix c) {
		// TODO Auto-generated method stub
		double[] re = new double[b.length];
		Matrix cc = c.inv();
		//System.out.println(cc);
		for(int i = 0; i<b.length; i++){
			double sum = 0;
			for(int j = 0; j<b.length; j++){
				sum = sum + b[j]*cc.getAsDouble(j, i);
			}
			re[i] = sum; 
			//System.out.println(re[i]);
		}
		
		return re;
	}

	public static double[] sort(double[] a,int low,int high){
        int start = low;
        int end = high;
        double key = a[low];
        
        
        while(end>start){
            //�Ӻ���ǰ�Ƚ�
            while(end>start&&a[end]>=key)  //���û�бȹؼ�ֵС�ģ��Ƚ���һ����ֱ���бȹؼ�ֵС�Ľ���λ�ã�Ȼ���ִ�ǰ����Ƚ�
                end--;
            if(a[end]<=key){
                double temp = a[end];
                a[end] = a[start];
                a[start] = temp;
            }
            //��ǰ����Ƚ�
            while(end>start&&a[start]<=key)//���û�бȹؼ�ֵ��ģ��Ƚ���һ����ֱ���бȹؼ�ֵ��Ľ���λ��
               start++;
            if(a[start]>=key){
                double temp = a[start];
                a[start] = a[end];
                a[end] = temp;
            }
        //��ʱ��һ��ѭ���ȽϽ������ؼ�ֵ��λ���Ѿ�ȷ���ˡ���ߵ�ֵ���ȹؼ�ֵС���ұߵ�ֵ���ȹؼ�ֵ�󣬵������ߵ�˳���п����ǲ�һ���ģ���������ĵݹ����
        }
        //�ݹ�
        if(start>low) sort(a,low,start-1);//������С���һ������λ�õ��ؼ�ֵ����-1
        if(end<high) sort(a,end+1,high);//�ұ����С��ӹؼ�ֵ����+1�����һ��
        return a;
    }

	private static boolean anyLower(double[] dx, double num) {
		// TODO Auto-generated method stub
		boolean re = false;
		for(int i = 0; i<dx.length; i++){
			if(dx[i]<num){
				re = true;
			}
		}
		return re;
	}

	private static double[] diff(double[] x) {
		// TODO Auto-generated method stub
		double[] re = new double[x.length-1];
		for(int i = 0; i<x.length-1; i++){
			re[i] = x[i+1] - x[i];
		}
		return re;
	}

}
