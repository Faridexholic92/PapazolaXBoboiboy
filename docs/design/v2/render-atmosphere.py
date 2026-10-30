from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter
import math,random
root=Path(__file__).parent
random.seed(17)
particles=[(random.uniform(.06,.94),random.uniform(.46,.91),random.uniform(1.0,2.0),random.uniform(0,math.tau),random.choice([1,2])) for _ in range(14)]
for n in range(240):
 phase=math.tau*n/240
 glow=Image.new('RGBA',(1280,720));d=ImageDraw.Draw(glow)
 sharp=Image.new('RGBA',(1280,720));s=ImageDraw.Draw(sharp)
 for x,y,r,p,speed in particles:
  xx=x*1280+7*math.sin(phase*speed+p)
  yy=y*720+12*math.cos(phase*speed+p)
  alpha=int(40+70*(.5+.5*math.sin(phase+p)))
  d.ellipse((xx-4*r,yy-4*r,xx+4*r,yy+4*r),fill=(255,186,65,alpha))
  s.ellipse((xx-r,yy-r,xx+r,yy+r),fill=(255,220,135,min(190,alpha+35)))
 out=Image.alpha_composite(glow.filter(ImageFilter.GaussianBlur(5)),sharp)
 out.save(root/'video-frames'/f'{n:03}.png',compress_level=1)
print('Prepared 240 periodic particle overlays at 1280×720.')
