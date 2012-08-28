package q.util.a.view;

import cn.xxd.tx.R;
import android.content.Context;
import android.widget.ListView;

public class QListView {
	
	public static final void init(Context ctx, ListView lv){
		lv.setCacheColorHint(0x00000000);
		lv.setDivider(ctx.getResources().getDrawable(R.drawable.abs__list_divider_holo_light));
		lv.setSelector(R.drawable.abs__list_pressed_holo_light);
	}

}
