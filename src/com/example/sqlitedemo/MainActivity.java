package com.example.sqlitedemo;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author ����ǿ
 * 
 *         SQLite ����ɾ���
 * 
 */
public class MainActivity extends Activity implements OnClickListener {

	private StudentModel mModel;

	private Button freshButton, inserButton, deleteButton, queryButton,
			updateButton;

	private StudentAdapter adapter;

	private Student student, currentStudent;

	private EditText inputNameText, inputAgeText;

	private String inputName, inputAge;

	private int selectItemPosition;

	private List<Student> students;

	private ListView mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		students = mModel.query();
		adapter = new StudentAdapter(students);
		mList.setAdapter(adapter);
		mList.setTextFilterEnabled(true);
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// mList.setItemChecked(0, true);

		mList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				StudentAdapter studentAdapter = (StudentAdapter) parent
						.getAdapter();
				currentStudent = studentAdapter.getStudents().get(position);
				Toast.makeText(
						MainActivity.this,
						"id:" + currentStudent.getStudentId() + ">name:"
								+ currentStudent.getStudentName() + ">age:"
								+ currentStudent.getStudentAge(),
						Toast.LENGTH_SHORT).show();
			}
		});

		// ע�ᣬ�������ݿ�仯���͹�����֪ͨ
		ContentResolver resolver = getContentResolver();
		resolver.registerContentObserver(StudentSettings.Students.CONTENT_URI,
				true, mFavoritesObserver);
	}

	private final ContentObserver mFavoritesObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			Toast.makeText(MainActivity.this, "ContentObserver:���������ݿ�仯!",
					Toast.LENGTH_SHORT).show();
			// �Զ����
			freshButton.performClick();
		}
	};

	private void init() {
		mModel = new StudentModel();
		mList = (ListView) findViewById(R.id.studentListId);
		freshButton = (Button) findViewById(R.id.freshButtonId);
		inserButton = (Button) findViewById(R.id.insertButtonId);
		deleteButton = (Button) findViewById(R.id.deleteButtonId);
		queryButton = (Button) findViewById(R.id.queryButtonId);
		updateButton = (Button) findViewById(R.id.updateButtonId);
		inputNameText = (EditText) findViewById(R.id.inputNameTextId);
		inputAgeText = (EditText) findViewById(R.id.inputAgeTextId);

		freshButton.setOnClickListener(this);
		inserButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		queryButton.setOnClickListener(this);
		updateButton.setOnClickListener(this);
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		inputName = inputNameText.getText().toString();
		inputAge = inputAgeText.getText().toString();

		int viewId = v.getId();
		switch (viewId) {
		// ˢ��
		case R.id.freshButtonId:
			students = mModel.query();
			adapter.setStudents(students);
			adapter.notifyDataSetChanged();
			break;
		// ��
		case R.id.insertButtonId:
			student = new Student();
			student.setStudentAge(Integer.valueOf(inputAge));
			student.setStudentName(inputName);
			mModel.insert(student);
			Toast.makeText(MainActivity.this,
					"������� > name = " + student.getStudentName(),
					Toast.LENGTH_SHORT).show();
			break;
		// ɾ > ��Id
		case R.id.deleteButtonId:
			student = new Student();
			student.setStudentId(currentStudent.getStudentId());
			mModel.delete(student);
			Toast.makeText(MainActivity.this,
					"ɾ������ > id = " + student.getStudentId(), Toast.LENGTH_SHORT)
					.show();
			break;
		// ��
		case R.id.queryButtonId:
			mModel.query();
			break;
		// �� > ��Id ������
		case R.id.updateButtonId:
			currentStudent.setStudentName(inputName);
			currentStudent.setStudentAge(Integer.parseInt(inputAge));
			mModel.update(currentStudent);
			Toast.makeText(MainActivity.this,
					"�޸����� > name = " + currentStudent.getStudentName(),
					Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		Log.d(StudentModel.TAG, "��ǰѡ��:" + selectItemPosition);
	}

	/**
	 * 
	 * @author ����ǿ
	 * 
	 *         �������ݿ�
	 * 
	 */
	class StudentModel extends BroadcastReceiver {
		public static final String TAG = "SQLiteDemo.test";

		private static final boolean LOGD = true;

		public StudentModel() {
			super();
		}

		/**
		 * ��
		 * 
		 * @param student
		 */
		public void insert(Student student) {
			final Context context = getApplicationContext();
			final ContentResolver contentResolver = context
					.getContentResolver();
			ContentValues contentValues = new ContentValues();
			contentValues.put(StudentSettings.BaseStudentColumns.Name,
					student.getStudentName());
			contentValues.put(StudentSettings.BaseStudentColumns.Age,
					student.getStudentAge());
			contentResolver.insert(StudentSettings.Students.CONTENT_URI,
					contentValues);
			if (LOGD) {
				Log.d(TAG, "�������");
			}
		}

		/**
		 * ɾ > ��Id
		 * 
		 * @param student
		 */
		public void delete(Student student) {
			final Context context = getApplicationContext();
			final ContentResolver contentResolver = context
					.getContentResolver();
			final Uri uriToDelete = StudentSettings.Students.getContentUri(
					student.getStudentId(), true);// ����Ϊtrue,����֪ͨ,ContentObserver��ɼ��������ݱ仯��Ϊfalse��������
			contentResolver.delete(uriToDelete, null, null);
			if (LOGD) {
				Log.d(TAG, "ɾ������");
			}
		}

		/**
		 * �� > ����
		 * 
		 * @return
		 */
		private List<Student> query() {
			Student student = null;
			List<Student> studentList = new ArrayList<Student>();
			final Context context = getApplicationContext();
			final ContentResolver contentResolver = context
					.getContentResolver();
			final Cursor c = contentResolver.query(
					StudentSettings.Students.CONTENT_URI, null, null, null,
					null);

			try {
				final int idIndex = c
						.getColumnIndexOrThrow(StudentSettings.BaseStudentColumns._ID);
				final int nameIndex = c
						.getColumnIndexOrThrow(StudentSettings.BaseStudentColumns.Name);
				final int ageIndex = c
						.getColumnIndexOrThrow(StudentSettings.BaseStudentColumns.Age);
				while (c.moveToNext()) {
					student = new Student();
					student.setStudentId(c.getInt(idIndex));
					student.setStudentName(c.getString(nameIndex));
					student.setStudentAge(c.getInt(ageIndex));
					studentList.add(student);
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				c.close();
			}
			if (LOGD) {
				Log.d(TAG, "��ѯ����");
			}
			return studentList;
		}

		/**
		 * �� > ��Id ������+������
		 * 
		 * @param student
		 */
		private void update(Student student) {
			/**
			 * ����1
			 */
			final Context context = getApplicationContext();
			final ContentResolver contentResolver = context
					.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(StudentSettings.BaseStudentColumns.Name,
					student.getStudentName());
			values.put(StudentSettings.BaseStudentColumns.Age,
					student.getStudentAge());
			String table = "students";
			String whereClause = "_id = ?";// ����ID�޸�����
			String[] whereArgs = { String.valueOf(student.getStudentId()) };
			contentResolver.update(StudentSettings.Students.CONTENT_URI,
					values, whereClause, whereArgs);
			if (LOGD) {
				Log.d(TAG, "�޸�����");
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

		}
	}

	private LayoutInflater mInflater;

	class StudentAdapter extends BaseAdapter {

		List<Student> students = null;

		Student student = null;

		public StudentAdapter(List<Student> students) {
			super();
			this.students = students;
			mInflater = LayoutInflater.from(MainActivity.this);
		}

		public List<Student> getStudents() {
			return students;
		}

		public void setStudents(List<Student> students) {
			this.students = students;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return students.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return students.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			student = students.get(position);
			CheckableLayout mCheckableLayout = (CheckableLayout) mInflater
					.inflate(R.layout.list_item, null);
			TextView studentIdText = (TextView) mCheckableLayout
					.findViewById(R.id.studentIdText);
			TextView studentNameText = (TextView) mCheckableLayout
					.findViewById(R.id.studentNameText);
			TextView studentAgeText = (TextView) mCheckableLayout
					.findViewById(R.id.studentAgeText);

			studentIdText.setText(String.valueOf(student.getStudentId()));
			studentNameText.setText(student.getStudentName());
			studentAgeText.setText(String.valueOf(student.getStudentAge()));

			return mCheckableLayout;
		}

	}
}
