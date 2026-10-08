package com.edii.eapregistration;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;

    private static final String HAL_LOGO_B64 =
            "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCABuAKADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9U6KKKAGNIqnB4+tRfb7fJBlQEdtwpZVXcc8Z/WvH9W0RpNVvH+yM4aQ8iwc/qG5q4R5tzKpPl2PXv7Qtv+eyf99CmvqdtH1lQD13D/GvGG0FtpP2J8D/AKcJP/iqs6Z4R+2TMXSOCOL5pDcWjxrgdeS1Pli205WsZupKO63R7AmowS5MbiQDqUII/nThdoVDc7cZz2r598T/ABdg8N3i6X4OsIWaRyJb25ZlhUjhiowScH/61eceItR8ba95kn/CYxuZl+eMI0aRe0eAc/jiuetiMJhpxjiaypqW19dtHte3zMFXxFXm+q0JVHFa2/4J9jLqVu7AK4Y4zxWT4l8d6L4Qs1udVvUtUdxHGh5eRicBVXqx+lfnt8QdeT4Xaaltb+JtR1XxddMXt4Ld2McHOCzjOck9BXdfCWbUtLktvGnxEu5Nd1hI0W0s7jJWzj7McZG85/AAd848rHZ1lmHnGlh6/PKTS+FpXfn1+R9bl2R5j9ReZ5tRdGnq0rpyl/ku7Pua1v0u7ZJlR0DruCuAGH15p7XSKOQ34CuV8D/EDR/HNg0+m3Cu6HbLBkboz2yB2I5zXL/G/wCNGm/B3wjc6vPtvL0OIYLNGw0jnPGPQda9+nhq1StHDxT5m0rep8tLG0FS+sKa5PX8EeqCZc4wR9aDMo9a+IvCP7Q/x2+IGnNrXh7wppz6RNIywzTTQIHVSQSN8qtwcjlR04qz4F/a38exfGXTfBXi3S7AXN1N5LwWjxkxAqW3lw5XaMevcV6jyjErn1Xu3vrroeXHOsM3FO/vO22h9qiUHsRS7xjODWUdbsIAhlvYUDAsN0gGRnqKVfEGmMmV1G1ccHImXp+ftXipSlrFaHuc6W71NLzRnGD9aYbtAxGDxjpg1Ti1zTrhxHHe28kn9xZVJ/LNfOn7SP7V6fCXULbRNAtINY1y5AJ/e/u4gzbUzjOSW4x+ddeGwtbFVFSpx1+448VjaODpe1qS0b9T6ZSdXBIBx70pnUAk5GPWvjay+LP7SF5aRzjwjpcauobEl1aqRnpkedVbXPjV+0J4f0m61PUPDejW1laxmWWeS5t2VFHc7ZSx69hXWstq83JzJv1OH+2KNuflaXofa1FFFeSe8V50XJPQ+vrXkesjbqt3/o2R5rc/ZJGzz6g165KMu2ODxXlOq2cJ1O7Jtix81skWZbPJ7hq3os5ayKEEfnTxoLZQzHAzZyiofjDqN34b8IyadpmxVEPmXRUsWcY5UYB610XhbTIJtVD/AGcIYwWBa1KfkSa818W+KmufEV/uYBUmdBuO7gMR247V8xxFnVTJ4UqlON22t9rdUd2X5VLNIzhTlyyS39dDyyfWNN03wct/rN3Fo6yYiUMpXDgZ2/UgivH9b+NYgtfJ0gtf6gSY40VGwvPGOOTXvHjPQ4PGWnwW1zbwHSW8xruWQ7Y4EB++x7MegHtXIfDP9m+yP2nxJ4duptTWCRlsY9Ri8ogdlVTwx9DmvisVh4Y+Uq2Ho2g/eSfaXvaejZ+wcM1crynA/wDCnNzqx002dtNX+LOe+C3wfV7xvFPjGSVtVuH3RWzglsYzuOenoB2xXvFjZaXHPJHa2TSy3K4MW7eRya880i91TXNZ/syCOQXvmGKUMPniYdRitCXxHdwalJ4e062ewu1cxzyXB2zSHHUnpt9Mdq+Rlh8fiJpwuo3skl1/rqdWa4142cqs6i5VG++kY+n9XHPqSfCHxPPr1vqXlyTy+S1pa4cbMD74zjIOTwehrxf45eI4PGfxKfS/DgGvyXflme7kO0byAXYDJ4QMoOO9dh8btmg+FP7RupFLDbbZc7Vabnoe+AOfwrzb4P3H/CL/AA78SfE/y98wvBomlNImUDbS0s59ssVHrgelf1Hl+Q06mXUa+KqSqV5KMdXZQb/+R7n8r4bOq2CzOpGMVHDQbleyvPyt0v2Nqx0rwb/bieF9KtvGniHVLdMuum3qxxkqA0hRc8KC2OepNWPgr+zxq3xT+J+oRXMOueGtHtI2mlv7uJ1uOWxFEGYAMeGyQSBiovhz8MNJ8XeDH1VfH1lpvj3Urhja6S1z5S7S33ZGHO5zlsDoNtfYnwV8F+NPhL8JNcj8Q3y63ro864tba3cyLGojG1AxHJyCfxr6DG414Sj9Xws/e0jrf5tHJhcEsfXeJxEPdu5aeeqiz4w/aQ0fTfAnjW08J+FdW1TUZNOt1guGuLpn3zyOcKMdyWHHo1T/ABt+F1r8F/CHheO78S6neeLtQtxcXNp5xWKCLG5gfmzwcAf7rVS+Csuj+Ifj0mt+PdWg0yCzee9kN0fv3YZQiOPbexHugr3e3/Zm0b4++NrvXX+Ky+LILe4je6ggtVLJEWZli3b+FO0jp2NejiMR/Z1WlTqy92mrt2+Jv5WPJw+GlmNOpVpx96o7LX4YrqYnwR/Zugu/hbF8Q/GviHWNPjKS38NnbztEqQhCiFs87jljjHOVrxr4WfDTT/iT4h8VeINY1e903wV4dhM0+otl5pJWY+TGuepwGJx0O31r6O/bm+JsXhTwdpPw90VlSe6RPMt4jhvJUqkUSjuXcqB/umvFvjJBcfCP4XeEvhpFi31HUtusavLL8nmzSfJGP91Act6bR61wYSpiK8FOc7TqN28orc78ZSo0pezhT9ymkv8AFJ7XM61uvhldTFm1H4g2+mmb7P8A2rI37nzMFim7dgsAMkDkA1yPxAuvDk6WEHg648Vag14TEq6w5VbssQsflqT8yl8DPTn2r6Uv/wBmvwp4h+Cnh+/i8bovhnQbWSS+NjEsiXUhwZvn3AhyPkBwe1cj+zD4NHxt+Olx4tvbcjw54XCG0gLBk83aVhT2CIHYj+8RWkMwpU4zrQb926+fQxngMRKpToTStLXbp1/E/RGiiivzk/UiCUfP1xmvJdX0YSandsIH5lY5WyJzz6lhmvWpR8x5rx7WbeYateBbNpB5rfN9jU559S4zW1LQ5a7Njwbp/wBlvbkCN0Z4iAzW/l4OPqc18w239o6j4m1LS/vXkN9cJKzPhY4xISHb0GMGvofRrmbSNSiums3SOP5pGW0UHb35DHFeMfHYHQpprnSLGSzsNfZrqa8xlp8fLsHoMAHHfdXyPE841MK8JCPNVdmv7sV8TPWySVSGYUavMlDWLvtd7X76jtF1eHXJk0aytYrrw/BLuubqc4N1MP48fyHYY9a9u8LXkMCwrGI40QYRIxgLXzn8Mb5ks/Kd47c53As2CR6Y9a9R0vxCEXbn5V6ba+o4Yyulh8uhFScpyWz6Lt6HjZ9mc6mPnTilGnHRW6vq35nWaR4LsfDPizxHrsLK41Zkl8vA/wBHIUA4/wB8jd+NcP8AEHQdN1nUbK7nj/0iNiI5onAYMDnn8DW5c+JsxYD8fWuN8S6lDeIGmAJjbzFYHkNjGa96OSR5fZwgrp3WnU8etns3UVRye1tH0OI+INppfivRTbSzWs9q0pEq3ikRiRe4/vde1dV8CdKs/B3wh8RXWsS6fD4YgvCzxz2xkiICLygx6np14riprSPxTqCQXZKWHmACNR+8mcnt+lVv2tvE1p8PPBGhfDHSsQh86hqA3jgnPloxz1+8T9Fr0cPiXj6ryeNRSmnedo6L/t5dbaWPna1CWDqPOJ0nGFrRu/iv/d9ep698N/F3wz8d+KINP8LS6O2usjXEP/EqdMbdoLAkDpla7P4j/GG1+FNzaWXiPxZZ2t1dIXjhjsXZtoPJO0Hjr+VfHP7KHxa8E/Bq61rXfEaXk2t3eyztfIhDLDABuZs56szEH2Ra4v4leMdS/aE+MIms0mlk1S5TT9PgOCVjyecA8YG9/wAK7HkcPrdSdW8aUFu1v6As9awsFTalVm+j2v3PvHwr4S8H/GPRIvE+m2ug6za3TuPtbaeULkHBGGAOc55rD1r4peBfgVq8+gR6xpeg3hCyzW1np8jAjsHKKeeTUnxS+Iej/sp/BvSPD2mNFJrQthbWVuvVnx88pHYbiTn3FfnpqkWveIZZPFOpQySWl7csn9ozSczTDBdUXqyqGHzDjkCsMsyyONl7SvK0L+73Z0Ztm08CnSw699L3rbI/THwoNG+NVjD4r0670nWDBMYI7+SwPmROmG24bHTcv51i/GXxR4U8Kz6bB8QdQ0q4up9zW0c2mvMwAwC3yg4HI6/0qr+xbaJ4d/Z0tL2ZhHHNNcXru3TbkAn8kr4++Iut3/7S/wC0FFY2bSOl9d/YLNQf9XbrkySD0wob8SPWuXB4KM8TXc5Wp076/kl6nbjMdOGGw6hG9SpbT82z7w8B2lp438DW03hm70uXwlfKwS3jsSsUq5wTg4PUHtXXeCvBSeContbOKwsrBju8iyt9gLdya3PCHhiy8I+GdM0awiWK0sYEhjCjHQdfx6/jWx5S+lfM1Kj1jF6H1NOltKersPooorM6SCQHece1eP6wY01W7DW3PmNybaM559d9ewspZmrh734czXd5NMtzbRiRi2Dagnn3zWkJJMwq05S2PHPiHrkOnxaVZeWsJvrpY93kohwOuCH966/xDdaB4q8KS+HfEMot5lUC0JRA7EcLsRSSeMcd629f+B1p4ns/smqPaXUGdwH2UAg+oOafofwYt/C8YXS3sbUAg5FmCSexyTXmujOFerVjb3rbrVK39aHqKphvqtOlJPmV72tZu+jvurI+OPGPh3xJ8KtXQanYXEWnv/x73DL8ki9iSMhW/wBk4Naej/FC3liVTKqkdcZ4/pX28PDA1TTJrHX2g1iKTI2vCFGPpXk3iX9kTwVrc8k9mbjSZHJJWJ8r+A6V+lYDOsEqccPXpqLSS5orsux+OZnw9myxNTF4Krzqbb5ZPq3rqeFz/Eq0VWHmg49M1yGsePrjV5UtLCJ5pHfAVQS7ZPZRzX0ZZ/sS+G0nX7RrF7cwjrHtC5/HNen+CPgr4Q+H7j+y9Mi+0rwbiXEkn0z1Fd9XPcupQcaCc5d9vzPKpcO55jZJYuShC/TX8jyP4PfCz/hDtNufG3i+JbM28BngtJTkoqjl37AnoBXxh4i1HVPj58aZDas8l/rV+ILZQwYxxZ69egQZ+pHrX6b/ABW+HbfE/wAE3ugR6vNpC3WA08MYc7R1XBI4PH5V5b8Df2ONI+DXi5vEL63Nrl4lu0FsslssKwbmBdhgnJO1R7bfevmstx+FwMK+IppKpP4bLbzufe5nl+MzGrh8PUlenTtdvqkfNn7Sf7Onhf4G+F9Plg13UL/Wr5ykUEgAVgoG9jzx1GB7Gsv9le20rwR/wkPxS8QKo0zQYzaacHGGnvZAC4UdyqiPn0kNfVPx2/ZMX42eL4NcufFlzpiwwLDHaLaLKiYJJYEsME5H5Vh6/wDsP2es+FvD/h608XXdjpWlo5aA2ayefM7MzStlhzyB9FFeqs6p1cHGhiazk38Tf5I8WeR16eMnicLRjGKXupd/5mfDnxN+I+p/FbxlqOtandKJ52BjiWQZtouyKM8AjPPrmneOfiHceMNL0PTzaWmn6boVs0FtDajC4YqzMccliVBr9Lfg1+zl4Y+D3hhtNigi1a8mkMtzf3UK75W7cc7QB0FebfFj9iTTvij4yvddj8SS6MtzGFFrDYoyqMEHB3D1rtpcRYP2jpyhaEVaPr/wTnrcOY+VN1VVvOVnL9TzXxz8SD8K/wBjrwZ4WtZfJ1jWbUqfmG5IC7M7e2dyD8a4j9jrxT4B+Gusax4s8XaulpqgX7Fp9u8TMUhJDSS8A/eKqP8AgJ9a98+Jf7Fdt8QNR0yZ/Ft1Ywafp0WnxWwsVdQEH3gS3c8muUX/AIJxacoiP/Cb3GRk4/s5Px53e1cFLH5bLAzw05tOcry0f3HXUwGZrGxxEYL3FaOq+/5n038N/jD4Z+Kkd6/hrVY9RSzKLOFRgYy2SucgdQp+mK7iKQsuWGD0ry/4GfBvS/gl4NbQ7C5e+lluGuLm9dAjzuR1IBOAAAMfWvSkn528D0z6V8TiFCM2qb0Tsj7rDSnKmva6SauW6KKKg6yORgoYngDvWM/i7S4nZGuSGU4IKN/hWvK6rnd0qDbEQSIwPqvWmpLqQ03sZw8X6W33bjcfZD/hXC/Hj4i6j4J8JWQ8PLDP4g1a7Sz0+Gc4VmIJJPoAO/vXpX2eOQ8RqSPUcivL/ir8ER8WvFmgS6tKP+Ed0yKZ2tY3KSvO+ArBh0AA/WurDOlGspVdlr6nBilWlQlClu9PQ2Pht8Qv+Ex+Fdj4jlHl3ItpftK7ThJ4iySj6B0Ye+K8M0L9qTxJd/DjxJe3lnAPEwuVGjwon7uaCQEwuf8Avh8+mK9c+F/wXu/ht4T8T+HItRW4028up5dNDElraKRRlDnr85c/jXKaF+y9LY3vhW+vdUSa40fRJdKkjjUiOdyzmOQ+43t+dejRqYKMqjmt3df5fPY82pTxzjTjB7Kz/wA/kbOi/tB2WmfDLwx4l8SQ3EX2+2M9zPb27vDEobbvZgMAdD9DWR4u/aHg8IfFdbK9nkm8Ky6FFqUbWdm8zsZJHUPlASEwoOTgVzt/+yf4g1HwroOjTa/Zzw2OmSae9vNCXhjfzpHWeMEffKuFOf7oxXZn4BagNZtrtNQtgkXhGHw4VIbmWPfiTp935+lXy4GLv/i/4BnCWYSjZ9OX8tTpPEPx28NeFY9Llu/tbadfxxSJqkds7WqLISI90gG3JIPGcjjOMjL2+O/hhPG1x4TT7dNrEKiR0gspZIwhTcHLqpUKegJPJBAryXxL+yxrOs21lZf21ZXMVtpVpZgXcTObWWFsloOMKH5yevSu4sPgXqFr4m8c6ouspav4i021soZbcHzbd4lcFsnsd/5VzSp4JRvGV/6R0qpjnNproGuftN6Ha+G/ElzbWOoS6vokSzyaXLaukxRywSTBH3TtOfTHPUVLD+0n4f07wx4Y1HVbXUrS71+1lubeyjsppZiYwu/5VUkD5wQSMEVxvhv9lvUdPuddkvtUsYzquiJpUosoyMSIzkTtkclgwznvntXU6Z8FtYS5+H11f6vavN4asbmyuGijKpOriNUKgjjCxjOeprWccCtn1f5f5mUZZg1b0Og1H4/+GbHwlpniNY7+80m9RpPtNtZSukCqdrGUhcJg8YOD19DR8Uvi/Z+Cfh5aeIdOVdTl1aaC00uNWAW4mmzsG7oBgEknsDXmer/ss63qOg6bpset2VxawQXsLW1zCWgVp5TIs6Lj/WDO3nt0r0PXfggnib4RaV4Ov7oxXOnLbyQX0IyYriL7sig/j+dYcmDjKnOLvd6+S8zoUsZKNSnJbLTz9DnPFPif4lfDv4V+J/EWv3miz3sK27WMNorBLctIqOHJA3ABxz7GuVsv2h/E+kaP4vtbu303xRqOkWNrepqGj5a3UTPtZZgo+VoxlyBn5Rmux1b4QePPHPw28R+GPFviHTL570WyWk8FuyhRHKHdpBjqwVRgZHWuoHwuPhXwPLpHgsaboWoBU/ePbgwSsMbg69wefcZrdVcNyONS0p36Ky6HN7LFOpGVK8YW2bu+p5tqPxn8RaH8Eb/xSPE/hvXro3UEUGoaZ89tAruqsJMDgqG5HXivRfg94svdf8MXmq6l4o0fxLEsoVLrRhmOLj5kOO/Irz3/AIZo1dvBniuJtVsE8S+I7yzvJ2hhMdlGbeTeqBQMkHoTjJzXqfww0HxJoOj3Nv4mm0iWXzP3X9kxFEVcfxAqOazxH1Z03y73NsNHFRqLm2t5nolFFFeQe+cp8QfFll4E8O6jruoTJHbWkRdtzAE8cKM+prxrTv2lGto7LU9cj0aLSZ0R5ZLDUkmuLNGA2+bHnJ687c4r1X4w2UGpfD7xHBNbrcKbR22MuckLx1618qal4NTWf7G0bS9CXUbS28N2d7e2No32ZyzoMmMjmRm/Q8V7mAoYetTft9NfI+azHE4mhWj7DXTz7n1dB8XvBffxZoijA4+3xD+bVY/4W14L2lv+Es0bAOCft0XB9PvV4lffCXXDYajqel6TpUWlixin0vRrjT0Fw7+UuY52YZVgcj14xXEfD/QNE8V6/rFhZSvfeLxpCSFbrShDb6fNuYtEYyPvZyDntiqhgsLJaTenoZTzHFR3gvvZ9Rv8W/BURAfxZoqk8/NfxD/2ann4reDQqn/hKtHw3Kn7dFz/AOPV8m6V4QvbHwP9shtLOygi1OWDXI003Nxp7mVizR7wNyAMpB9CMcVmav4E0/SG8fWzRLqskV5p3k37W4UusiIxCgdOtbQy/CzbXtHp5IyqZpjIWtSWvmz7Ci+LXguYgR+K9GcnoFv4j/7NWjf+NdB0yzt7u71mwtLW4G6Gea5REkHXKknB6jp618y/Fv4Savpuk+JdQuodJh0aG4h+w29lp8ZmkhLJkFuoO4t+FelfGnwMuseDPD1zaaYl/b6JdxXU1hEhYvAEwyqo6nkcegrheHoWhJTer8ux6CxWKbnF01ou77nYav8AFLwv5EotPE+h/bTGfJ82+ixuI4HXocVzfgj48eHtb0mSLV9Z0rSdZtZDBc2kt7GU3DoyNuwysMHI9x1Brw/WvhasPwzl13QH0vUNLv76aXUPs9iJJILd3AUKMZDRAEMPUn0q3ofw0S68b+NLHwrY6ZNNFpOlNY3F3bq8L5Eu9serLg5rtWDwii05P7kebLH41yT5Evmz1ix+Oc9zrkNnPHo1vafaXR9TOrQmBocDGwBtxfPUYrP1/wDaLEnifUdO8OSaFewWKJvub7VYoVmkOcxx/N1AAznA5FeU+I/h1Fo174+ttVtbTUrmHQrCWGW3sliWJmnl3Kqrxng89SMU7UPDttouoeKbjSfDtpLNFpOkM0jWYk+zoxm82UKfvsBjI/wrWOEwj1T/AC8v8zKeOxkN1+fp+h734J+P/hTxRYmS71uw0i9gk8mezuruNSj/AOyd2GB7EV1P/C1/BvmbP+Eq0cv/AHft0Wf/AEKvki68OaR5N/deGJG8b2bJJLq02lWcaSxSeXiBD2EeBISByDjirN38LLjTfhN4Z1NzBeaRdyxf2h9otAZbFTJkOhHYEbD/AL9RVy/CPX2jXy/r8TSlmeMWipp/Nn15o/jfQPEMrx6brOn30iqXK21ykhCjqcA9Of1rxW9/aVa71XWBokei3Gn6bd/ZyLzU0gnmcD5jGrEZ7gZ6mpP2ePCyaL4r8e+dbWT3Hm28cU1lb+WhhKMQBkDPYnFef6r4dt7Cw8TT2ul21lG/iprW51QWayvaQNCCr7T283Yuf9us6WFw8K04Sd7cv4q5tVxmKqUYVILfm79HY9fm/aS8H/8ACAXHiVdVs98MTFrDz0+0FwQDHtzknJHTtWBpv7S509rS78Qro0Oi3e1Rc6dqcc7wFiAokTOQPm5IBA715OfAM2kXHiq11K3stbebQJrq+uY7JUjspgQLZUI6F1MhPuBWl4u+EWt22k6trul6LocOhnwwoDTRkTIwTLlQBnfjOCePeun6pgo3Unvtt5HKsbjp2cV2vv5/5H2Ziql3OlskkjnCoNxY9gBmrdZusQG6067RTjdG6nP+7Xyq1kk9j7CbcYSlHc5OP4s+Fb3T7G9XVYZLO+glnhPUSIn3/wAAK4nx3qnwu8avK2qXcbXWk2kV2ZbZmimiglAdCGHO0hgce9eV+DvgddaL4f8AC8n9pmeY6PeQyQyzSPAhZOPLUj5R61N4r+CF5qWk6jf2l7bW2qJbadD5x3Ye3W2iSSNuO5UkfWvoaeFw8Zte0Z8zVxmKml+7TPYrj4reC7538Haf4lNhqCj7DDPGxyJFUDYsp4Lj0zmuD8M2GlJ8QNM8Vaz4xtb2RbiawtZre3FvJdSIdrpMwHzbSpGD0Oa5i38B6prfijWfBpms4bG91cau12pfzo0AGEXjG7jrVrSPAmqJ48Xw7JLYvYaJqV5rhl+YyXCzu0gjYYwCNxBNbxoUoKSi3qcs8RWnyylFJpnsMnx78DWml3Woy6gV02GUQyT/AGV/LZj3B2/MPccVc074w+CdXOirbahAx1wn7CrxMpmCnHceoOK8S8AeGtV1v4feOYIZ4LPRbiFYbLTDK8sdpIMbypYZCnqFFJ4m+E+veJNTOrRazb2I8OWenwW0SIT5hjjDsxOPlyZWHHYCsY4Ohzcs5tb/AJaG7x+JUL04J/8ADnu8/wAYPBzy6hbzaxbu9jfpp9whGdk7gMqEe4I59RWv4i8d6D4X1bS9L1PUYra91Z2SzhfrIVA3AenUV82678Gby51aTXbW8t7aebXo5rxQWxcQlVeNTx95Sz8+9bvxe+HWs+PfH2tatBq8VhD4ftbaO2g2lvMbcZGYnHyE7gMr1AFc6wlC8F7R6rX1OqWNxPLP92t+/Q9B8MeOPh54P1HWvDelXIhuLaaa+1CJYWKLI2HdmOMZwV4q9pXxz8A6hpN/qlprVtHbWYjW4LqY5AHz5fykAnODj1wfSvD7Xw9ff8LF8YW/mJu161NyuLiTyo90EYbMWNpOVOD6YqOL4P6h4dsbpory11CewGnask9+XeSXy4nQwsccIM5UDpk12PBYdr43fT8bXONY/F07Wprr/wAA95s/jP4HvtJtNTGs2ptr68/s5HkGG88DPlMp+6e+D6j1q1qfxS8HaJpF1rF1qNvDaQ3Y0yeUjkSjkRn6Bs/SvBU+Fs3jw2+v6o9rGniDWVnls4GbZCqQFEZSR9/cN2fYVStvgr4g8Q6PaeHrrX4EkivdQ1Ca7EZkMrBI4o8qwxnaGyfU5qFgaH2pNdy3mOLeipr7z6Nh8feErFNWjju7aFNOhiuLtY0wFSUny24HIODWDN+0D8PTqz6H/aiG+WLzmthbuQI8Ft3TGMDrXh974d1rTreLT1ms7mTxTp1vpFxPKzhofs0r4ZcDnKv36EV6RffDe6tPH2qSJcW7wHwd/Zqhgd29dwDHjGOfrWTw+GjtJv8A4Zfrc1hi8TNWlTX9N2+5WO0l+NXgrSptOg/tOJrnVAjW8dvGWkcE7VYqBwO2T6VpaF8RvDHirXtQ0nTZxd3NqrPOEgbyyVIB+fG0kZHAOa8O0CDV/hVrWj3Qh07U4NZtLWymWYsHhKMy5Q45BEnQ+lbXwd0zUIvid4mGnvDpmgQLJFLpccrvG87MCJVBGE4zkDrmlPB01zcr2X6kU8yqS5VK2vkeuH4jeFR4fi1j7bCunXN0th5rJjdKW2hMEdd1Z998ZPBtvY6pNLq1ultp04srkt1SVvupj35rwbStM1fU/DNz4MZbGKHT9ahvFuEdyz4ulbBGKr/8KIutWGq3lzeWzR3i3N5LB8xV7lGVYXPHIUM/44rall1GabqNjrZnWi0qaR//2Q==";

    private static final String EDII_LOGO_B64 =
            "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";

    public static class Result {
        public final String fileName;
        public final String base64;
        public final String savedLocation;

        Result(String fileName, String base64, String savedLocation) {
            this.fileName = fileName;
            this.base64 = base64;
            this.savedLocation = savedLocation;
        }
    }

    public static Result createAndSave(Context context, JSONObject data, String requestId) throws Exception {
        byte[] bytes = createPdfBytes(data);

        String cleanName = sanitize(data.optString("name", "Participant"));
        String place = sanitize(data.optString("place", data.optString("city", "Location")));
        String locationSerial = sanitize(
                data.optString("individualSerialNumber", "").replace("/", "-"));
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

        String fileName;
        if (!locationSerial.isEmpty()) {
            fileName = cleanName + "_" + place + "_" + locationSerial + ".pdf";
        } else {
            fileName = cleanName + "_" + place + "_" + stamp + ".pdf";
        }

        String location = saveToDevice(context, bytes, fileName);
        String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return new Result(fileName, b64, location);
    }

    private static byte[] createPdfBytes(JSONObject data) throws Exception {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas c = page.getCanvas();

        Paint regular = paint(12f, false);
        Paint bold = paint(12f, true);
        Paint title = paint(14.04f, true);
        Paint small = paint(11.04f, false);
        Paint smallBold = paint(11.04f, true);
        Paint value = paint(10.2f, false);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(1f);

        // Exact source form layout
        drawLogo(c, HAL_LOGO_B64, new RectF(21.75f, 7.5f, 116.25f, 58f));
        drawLogo(c, EDII_LOGO_B64, new RectF(483.1f, 6.75f, 588.04f, 55.55f));

        drawCentered(c, "Entrepreneurship Awareness Programme (EAP)", title, 300f, 20.5f);
        drawCentered(c, "Organized by", regular, 300f, 36.5f);
        drawCentered(c, "Hindustan Aeronautics Limited (HAL)", bold, 300f, 51.5f);
        drawCentered(c, "In Collaboration with", regular, 300f, 66.5f);
        drawCentered(c, "Entrepreneurship Development Institute of India (EDII)", bold, 300f, 81.5f);
        drawCentered(c, "Website: www.ediindia.org, www. ediindia.ac.in", small, 300f, 95.5f);

        drawCentered(c, repeat('*', 88), small, 300f, 117f);
        drawCentered(c, "Registration Form", bold, 302f, 132f);

        c.drawText("Date:", 83.06f, 148f, regular);
        drawFit(c, safe(data.optString("eapDate")), 118f, 148f, 180f, value, 8f);

        c.drawText("Place:", 410.02f, 148f, regular);
        drawFit(c, safe(data.optString("place", data.optString("city"))), 449f, 148f, 118f, value, 8f);

        drawCentered(c, repeat('*', 88), small, 300f, 161f);

        drawPhotoOrPlaceholder(c, data.optString("photoBase64", ""), line, smallBold);

        c.drawText("1)", 89.3f, 189f, regular);
        c.drawText("Full Name:", 143.3f, 189f, regular);
        drawFit(c, safe(data.optString("name")), 216f, 189f, 265f, value, 7.5f);

        c.drawText("2)", 89.3f, 215f, regular);
        c.drawText("Gender:", 143.3f, 215f, regular);
        drawFit(c, safe(data.optString("gender")), 195f, 215f, 285f, value, 8f);

        c.drawText("3)", 89.3f, 237f, regular);
        c.drawText("Date of Birth:", 143.3f, 237f, regular);
        drawFit(c, safe(data.optString("dob")), 229f, 237f, 250f, value, 8f);

        c.drawText("4)", 89.3f, 259f, regular);
        c.drawText("Father's/Husband's/Mother's Name:", 143.3f, 259f, regular);
        drawFit(c, safe(data.optString("guardianName")), 365f, 259f, 116f, value, 7f);

        c.drawText("5)", 89.3f, 281f, regular);
        c.drawText("Full Address:", 143.3f, 281f, regular);
        drawFit(c, safe(fullAddress(data)), 228f, 281f, 252f, value, 6.8f);

        c.drawText("6)", 89.3f, 303f, regular);
        c.drawText("Mobile Phone:", 143.3f, 303f, regular);
        drawFit(c, safe(data.optString("mobile")), 232f, 303f, 247f, value, 8f);

        c.drawText("7)", 89.3f, 325f, regular);
        c.drawText("Alternative Phone No.:", 143.3f, 325f, regular);
        drawFit(c, safe(data.optString("alternateMobile")), 280f, 325f, 200f, value, 8f);

        c.drawText("8)", 89.3f, 347f, regular);
        c.drawText("Email ID:", 143.3f, 347f, regular);
        drawFit(c, safe(data.optString("email")), 204f, 347f, 276f, value, 7.5f);

        c.drawText("9)", 89.3f, 369f, regular);
        c.drawText("Aadhar No.:", 143.3f, 369f, regular);
        drawFit(c, safe(data.optString("idNumber")), 219f, 369f, 260f, value, 8f);

        c.drawText("10)", 89.3f, 392f, regular);
        c.drawText("Highest Educational Qualification:", 143.3f, 392f, regular);
        drawFit(c, safe(data.optString("education")), 351f, 392f, 128f, value, 6.8f);

        c.drawText("11)", 89.3f, 414f, regular);
        c.drawText("Occupation:", 143.3f, 414f, regular);
        drawFit(c, safe(data.optString("occupation")), 219f, 414f, 260f, value, 8f);

        c.drawText("12)", 89.3f, 436f, regular);
        c.drawText("Income (Individual):", 143.3f, 436f, regular);
        drawFit(c, safe(data.optString("individualIncome")), 263f, 436f, 216f, value, 8f);

        c.drawText("13)", 89.3f, 463f, regular);
        c.drawText("Category:", 143.3f, 463f, regular);
        drawCheckOption(c, 202.61f, 449.6f, "GEN", 216.05f, 463f,
                "GEN".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 247.01f, 449.6f, "EWS", 261.89f, 463f,
                "EWS".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 293.83f, 449.6f, "SC", 308.59f, 463f,
                "SC".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 329.23f, 449.6f, "ST", 343.99f, 463f,
                "ST".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 363.19f, 449.6f, "OBC", 378.10f, 463f,
                "OBC".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 409.30f, 449.6f, "MINORITY", 424.06f, 463f,
                "MINORITY".equalsIgnoreCase(data.optString("category")), regular, line);

        c.drawText("14)", 89.3f, 486f, regular);
        c.drawText("Intention for taking part in the EAP:", 143.3f, 486f, regular);
        drawBox(c, 364.5f, 477.2f, 12f, line, "Employment".equalsIgnoreCase(data.optString("intention")));
        c.drawText("a) Employment", 382.18f, 486f, regular);
        drawBox(c, 363.75f, 490.73f, 12f, line,
                "Self Employment".equalsIgnoreCase(data.optString("intention")) ||
                "Self-employment".equalsIgnoreCase(data.optString("intention")));
        c.drawText("b) Self-employment", 383.14f, 500f, regular);

        c.drawText("13)", 86.18f, 523f, regular);
        c.drawText("In which sector would you like to start business?", 143.06f, 523f, regular);

        String selectedSector = data.optString("sector", data.optString("sectors", ""));
        drawBox(c, 166.3f, 537f, 12f, line, "Fashion Technology".equalsIgnoreCase(selectedSector));
        c.drawText("Fashion technology", 204.77f, 547f, regular);
        drawBox(c, 166.3f, 551.05f, 12f, line, "Food Processing".equalsIgnoreCase(selectedSector));
        c.drawText("Food processing", 204.77f, 561f, regular);
        drawBox(c, 166.3f, 565.2f, 12f, line, "Jute Bag Manufacturing".equalsIgnoreCase(selectedSector));
        c.drawText("Jute Bag manufacturing", 204.77f, 575f, regular);
        drawBox(c, 166.3f, 579.25f, 12f, line, "Beautician".equalsIgnoreCase(selectedSector));
        c.drawText("Beautician", 204.77f, 589f, regular);

        c.drawText("14)", 86.18f, 611f, regular);
        c.drawText("Do you want to attend Micro Skill Entrepreneurship Development", 143.06f, 611f, regular);
        c.drawText("Programme (MSDP) to understand business?", 142.94f, 625f, regular);

        boolean msdpYes = "Yes".equalsIgnoreCase(data.optString("msdpInterest"));
        boolean msdpNo = "No".equalsIgnoreCase(data.optString("msdpInterest"));
        drawBox(c, 433.28f, 615.83f, 12f, line, msdpYes);
        c.drawText("Yes", 451.06f, 625f, regular);
        drawBox(c, 505.73f, 615.83f, 12f, line, msdpNo);
        c.drawText("No", 523.44f, 625f, regular);

        c.drawText("I declare that the above information provided by me is completely correct. I will be",
                57.86f, 654f, regular);
        c.drawText("responsible if any discrepancy is detected.", 114.62f, 669f, regular);

        RectF sign = new RectF(408f, 686.15f, 554.25f, 729.65f);
        c.drawRect(sign, line);
        drawSignature(c, data.optString("signatureBase64", ""), sign, smallBold);

        drawFit(c, safe(data.optString("totalSerialNumber", "EAP/2026/N")),
                72.02f, 797f, 180f, small, 8f);
        drawFit(c, safe(data.optString("individualSerialNumber", "EAP/2026/K-S/N")),
                438.46f, 797f, 145f, small, 8f);

        document.finishPage(page);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.writeTo(out);
        document.close();
        return out.toByteArray();
    }

    private static Paint paint(float size, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return p;
    }

    private static void drawLogo(Canvas c, String b64, RectF rect) {
        try {
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) c.drawBitmap(bitmap, null, rect, new Paint(Paint.ANTI_ALIAS_FLAG));
        } catch (Exception ignored) {}
    }

    private static void drawSignature(Canvas c, String b64, RectF box, Paint label) {
        if (b64 != null && !b64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    float availableW = box.width() - 8f;
                    float availableH = box.height() - 8f;
                    float scale = Math.min(availableW / bitmap.getWidth(), availableH / bitmap.getHeight());

                    float drawW = bitmap.getWidth() * scale;
                    float drawH = bitmap.getHeight() * scale;
                    float left = box.left + (box.width() - drawW) / 2f;
                    float top = box.top + (box.height() - drawH) / 2f;

                    RectF destination = new RectF(left, top, left + drawW, top + drawH);
                    c.drawBitmap(bitmap, null, destination, new Paint(Paint.ANTI_ALIAS_FLAG));
                    return;
                }
            } catch (Exception ignored) {}
        }
        c.drawText("SIGNATURE", box.left + 46, box.top + 27, label);
    }

    private static void drawPhotoOrPlaceholder(Canvas c, String b64, Paint border, Paint label) {
        RectF box = new RectF(492.75f, 178.43f, 568.20f, 279.68f);
        c.drawRect(box, border);

        if (b64 != null && !b64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    float scale = Math.max(box.width() / bitmap.getWidth(), box.height() / bitmap.getHeight());
                    float drawW = bitmap.getWidth() * scale;
                    float drawH = bitmap.getHeight() * scale;
                    float left = box.left + (box.width() - drawW) / 2f;
                    float top = box.top + (box.height() - drawH) / 2f;
                    RectF destination = new RectF(left, top, left + drawW, top + drawH);

                    int save = c.save();
                    c.clipRect(box.left + 1f, box.top + 1f, box.right - 1f, box.bottom - 1f);
                    c.drawBitmap(bitmap, null, destination, new Paint(Paint.ANTI_ALIAS_FLAG));
                    c.restoreToCount(save);
                    return;
                }
            } catch (Exception ignored) {}
        }

        c.drawText("PHOTO", 510.48f, 229f, label);
    }

    private static void drawCentered(Canvas c, String text, Paint p, float centerX, float baselineY) {
        c.drawText(text, centerX - p.measureText(text) / 2f, baselineY, p);
    }

    private static void label(Canvas c, Paint p, String text, float x, float y) {
        c.drawText(text, x, y, p);
    }

    private static void drawFit(Canvas c, String text, float x, float y, float maxWidth, Paint base, float minSize) {
        if (text == null || text.trim().isEmpty()) return;
        Paint p = new Paint(base);
        while (p.measureText(text) > maxWidth && p.getTextSize() > minSize) {
            p.setTextSize(p.getTextSize() - 0.25f);
        }
        String fitted = text;
        if (p.measureText(fitted) > maxWidth) {
            while (fitted.length() > 3 && p.measureText(fitted + "...") > maxWidth) {
                fitted = fitted.substring(0, fitted.length() - 1);
            }
            fitted = fitted + "...";
        }
        c.drawText(fitted, x, y, p);
    }

    private static void drawCheckOption(Canvas c, float boxX, float boxY, String text, float textX, float textY,
                                        boolean checked, Paint textPaint, Paint linePaint) {
        drawBox(c, boxX, boxY, 10, linePaint, checked);
        c.drawText(text, textX, textY, textPaint);
    }

    private static void drawBox(Canvas c, float x, float y, float size, Paint linePaint, boolean checked) {
        RectF r = new RectF(x, y, x + size, y + size);
        c.drawRect(r, linePaint);
        if (checked) {
            Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
            tick.setStrokeWidth(1.6f);
            tick.setStyle(Paint.Style.STROKE);
            c.drawLine(x + 2, y + size * 0.55f, x + size * 0.42f, y + size - 2, tick);
            c.drawLine(x + size * 0.42f, y + size - 2, x + size - 2, y + 2, tick);
        }
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && needle != null &&
                haystack.toLowerCase(Locale.ENGLISH).contains(needle.toLowerCase(Locale.ENGLISH));
    }

    private static String fullAddress(JSONObject data) {
        String full = data.optString("fullAddress", "");
        if (!full.trim().isEmpty()) return full;

        StringBuilder sb = new StringBuilder();
        appendPart(sb, data.optString("village"));
        appendPart(sb, data.optString("panchayat"));
        appendPart(sb, data.optString("block"));
        appendPart(sb, data.optString("city"));
        appendPart(sb, data.optString("state"));
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String s) {
        if (s == null || s.trim().isEmpty() || "Select".equalsIgnoreCase(s.trim())) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(s.trim());
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private static String saveToDevice(Context context, byte[] bytes, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EAP Registrations");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Unable to create PDF in Downloads.");

            try (OutputStream os = resolver.openOutputStream(uri)) {
                if (os == null) throw new Exception("Unable to open PDF output.");
                os.write(bytes);
            }

            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return "Downloads/EAP Registrations/" + fileName;
        }

        File base = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (base == null) base = context.getFilesDir();
        File folder = new File(base, "EAP Registrations");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new Exception("Unable to create PDF folder.");
        }
        File file = new File(folder, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
        return file.getAbsolutePath();
    }

    private static String sanitize(String value) {
        if (value == null) return "";
        String cleaned = value.trim().replaceAll("[^A-Za-z0-9_ ]", "");
        cleaned = cleaned.replaceAll("\\s+", "_");
        if (cleaned.length() > 35) cleaned = cleaned.substring(0, 35);
        return cleaned;
    }
}
