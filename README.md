# EasyCameraExt

根据 https://github.com/Glamdring/EasyCamera 改写。

##使用：
 
#### (1) 启动：
 
 `startActivityForResult( new Intent(mContext, EasyCameraActivity.class), REQUEST_CODE_CAMERA);`

####  (2) 获取拍照结果(目前仅返回照片在sd卡上的目录)：
```
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data.hasExtra(EasyCameraActivity.RESULE_IMAGE_PATH)){
			String imagePaht = data.getStringExtra(EasyCameraActivity.RESULE_IMAGE_PATH);
		}
}
```
#### 界面效果:

<img  src="http://img.blog.csdn.net/20150729150309253" height="512" width="384"/>

##License
Copyright (C) 2014 tvbarthel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
